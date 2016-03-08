/* Copyright (c) 2016 W.T.J. Riezebos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.riezebos.thoth.content.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManagerBase;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.Revision;
import net.riezebos.thoth.content.versioncontrol.Revision.Action;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.content.versioncontrol.SimpleDiffFormatter;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.BasicFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileSystem;
import net.riezebos.thoth.util.PagedList;
import net.riezebos.thoth.util.ThothCoreUtil;
import net.riezebos.thoth.util.ThothUtil;

/**
 * Support GIT based version control as the manager for the content
 * 
 * @author wido
 */
public class GitContentManager extends ContentManagerBase {

  private static final Logger LOG = LoggerFactory.getLogger(GitContentManager.class);
  private static final String HEAD_TREE = "HEAD^{tree}";

  public GitContentManager(ContextDefinition contextDefinition, ThothEnvironment context) throws ContentManagerException {
    super(contextDefinition, context);
    validateContextDefinition(contextDefinition);
    setFileSystem(createFileSystem());
  }

  protected FileSystem createFileSystem() throws ContextNotFoundException {
    return new BasicFileSystem(getContextFolder());
  }

  @Override
  public boolean supportsVersionControl() {
    return true;
  }

  @Override
  protected synchronized String cloneOrPull() throws ContentManagerException {
    StringBuilder log = new StringBuilder();
    try {
      Configuration config = getConfiguration();
      RepositoryDefinition repositoryDefinition = getContextDefinition().getRepositoryDefinition();
      String repositoryUrl = repositoryDefinition.getLocation();
      String workspaceLocation = config.getWorkspaceLocation();
      if (StringUtils.isBlank(workspaceLocation))
        throw new IllegalArgumentException("No environment variable or system variable named " + Configuration.WORKSPACELOCATION + " was set");
      File file = new File(workspaceLocation);
      if (!file.isDirectory()) {
        severe(log, "The library path " + workspaceLocation + " does not exist. Cannot initialize content manager");
      } else {
        try {
          String contextFolder = getContextFolder();
          File target = new File(contextFolder);
          CredentialsProvider credentialsProvider = getCredentialsProvider();

          if (target.isDirectory()) {
            try (Git repos = getRepository()) {

              Repository repository = repos.getRepository();
              ObjectId oldHead = repository.resolve(HEAD_TREE);

              PullResult pullResult = repos.pull().setCredentialsProvider(credentialsProvider).call();
              ObjectId newHead = repository.resolve(HEAD_TREE);
              boolean changes = (oldHead == null && newHead != null)//
                  || (oldHead != null && !oldHead.equals(newHead));
              if (changes)
                notifyContextContentsChanged();

              String message = pullResult.isSuccessful() ? ": Pull successful, " : ": Pull of failed, ";
              message += changes ? CHANGES_DETECTED_MSG : NO_CHANGES_DETECTED_MSG;
              info(log, getContextName() + message);

              setLatestRefresh(new Date());
            } catch (Exception e) {
              severe(log, e);
            }
          } else {
            info(log, getContextName() + ": Cloning from " + repositoryUrl + " to " + contextFolder);
            target.mkdirs();
            try (Git result = Git.cloneRepository()//
                .setURI(repositoryUrl)//
                .setBranch(getBranch())//
                .setCredentialsProvider(credentialsProvider)//
                .setDirectory(target).call()) {
              info(log, getContextName() + ": Cloned repository: " + result.getRepository().getDirectory());
              setLatestRefresh(new Date());
              notifyContextContentsChanged();
            } catch (Exception e) {
              severe(log, e);
            }
          }
        } catch (Exception e) {
          severe(log, e);
        }
      }

    } catch (Exception e) {
      throw new ContentManagerException(e);
    }

    return log.toString().trim();
  }

  public String getBranch() {
    return getContextDefinition().getBranch();
  }

  protected Git getRepository() throws ContextNotFoundException, IOException {
    String contextFolder = getContextFolder();
    File target = new File(contextFolder);
    return Git.open(target);
  }

  @Override
  public PagedList<Commit> getCommits(String path, int pageNumber, int pageSize) throws ContentManagerException {
    path = ThothUtil.stripPrefix(path, "/");
    try (Git repos = getRepository()) {
      Repository repository = repos.getRepository();
      List<Commit> commits = new ArrayList<>();
      LogCommand log = repos.log();
      if (path != null)
        log = log.addPath(path);
      Iterable<RevCommit> revisions = log.call();
      Iterator<RevCommit> iterator = revisions.iterator();

      // First skip over the pages we are not interested in
      int skipCount = (pageNumber - 1) * pageSize;
      while (skipCount-- > 0 && iterator.hasNext())
        iterator.next();

      // Now add the revisions
      while (iterator.hasNext() && commits.size() < pageSize) {
        RevCommit revCommit = iterator.next();
        commits.add(getCommit(repository, revCommit, path));
      }
      PagedList<Commit> pagedList = new PagedList<>(commits, iterator.hasNext());
      return pagedList;
    } catch (IOException | GitAPIException e) {
      throw new ContentManagerException(e);
    }
  }

  protected Commit getCommit(Repository repository, RevCommit revCommit, String path)
      throws MissingObjectException, IncorrectObjectTypeException, IOException, UnsupportedEncodingException {
    path = ThothUtil.stripPrefix(path, "/");

    Commit commit = new Commit();
    commit.setAuthor(revCommit.getAuthorIdent().getName());
    commit.setTimestamp(new Date(((long) revCommit.getCommitTime()) * 1000));
    commit.setMessage(revCommit.getFullMessage());
    commit.setShortMessage(revCommit.getShortMessage());
    commit.setId(ObjectId.toString(revCommit.getId()));

    try (RevWalk revWalk = new RevWalk(repository); SimpleDiffFormatter df = new SimpleDiffFormatter()) {
      RevCommit[] parents = revCommit.getParents();
      if (parents != null && parents.length > 0) {
        RevCommit parent = revWalk.parseCommit(parents[0].getId());
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        List<DiffEntry> diffs = df.scan(parent.getTree(), revCommit.getTree());
        for (DiffEntry diff : diffs) {
          if (path == null || path.equals(diff.getNewPath()) || path.equals(diff.getOldPath())) {
            Revision fileRevision = createFileRevision(diff, df, revCommit);
            commit.addRevision(fileRevision);
          }
        }
      } else {
        // Initial version; no parent
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
          RevTree tree = revCommit.getTree();
          treeWalk.addTree(tree);
          treeWalk.setRecursive(true);
          treeWalk.setFilter(PathFilter.create(path));
          if (treeWalk.next()) {
            Revision fileRevision = new Revision(Action.ADD, path);
            fileRevision.setCommitId(ObjectId.toString(revCommit.getId()) + "/" + path);
            commit.addRevision(fileRevision);
          }
        }
      }
    }
    return commit;
  }

  @Override
  public SourceDiff getDiff(String diffSpec) throws ContentManagerException {

    int idx = diffSpec.indexOf('/');
    if (idx == -1)
      return null;

    String id = diffSpec.substring(0, idx);
    String path = diffSpec.substring(idx + 1);

    if (id == null || ObjectId.zeroId().equals(ObjectId.fromString(id)))
      return null;

    SourceDiff result = null;

    try (Git git = getRepository(); RevWalk revWalk = new RevWalk(git.getRepository()); SimpleDiffFormatter df = new SimpleDiffFormatter()) {
      Repository repository = git.getRepository();

      RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(id));

      RevCommit[] parents = revCommit.getParents();
      if (parents != null && parents.length > 0) {
        RevCommit parent = revWalk.parseCommit(parents[0].getId());
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        List<DiffEntry> diffs = df.scan(parent.getTree(), revCommit.getTree());
        for (DiffEntry diff : diffs) {
          if (path.equals(diff.getNewPath())) {

            result = createSourceDiff(repository, revCommit, diff);
            break;
          }
        }
      }
      return result;
    } catch (Exception e) {
      throw new ContentManagerException(e);
    }
  }

  protected SourceDiff createSourceDiff(Repository repository, RevCommit revCommit, DiffEntry diff)
      throws MissingObjectException, IOException, UnsupportedEncodingException {
    SourceDiff result;
    ObjectId oldId = diff.getOldId().toObjectId();
    ObjectId newId = diff.getNewId().toObjectId();

    String oldSource = getContents(repository, oldId);
    String newSource = getContents(repository, newId);

    if (oldId.equals(newId))
      oldSource = "";

    PersonIdent authorIdent = revCommit.getAuthorIdent();
    String author = authorIdent == null ? "Unknown" : authorIdent.getName();
    Date timeModified = new Date(((long) revCommit.getCommitTime()) * 1000);

    result = new SourceDiff(author, oldSource, newSource, timeModified);
    result.setCommitMessage(revCommit.getFullMessage());
    return result;
  }

  protected Revision createFileRevision(DiffEntry diff, SimpleDiffFormatter df, RevCommit revCommit) throws IOException, UnsupportedEncodingException {
    Action action = translateAction(diff.getChangeType());
    Revision fileRevision = new Revision(action, action.equals(Action.DELETE) ? diff.getOldPath() : diff.getNewPath());
    fileRevision.setMessage(revCommit.getFullMessage());
    fileRevision.setCommitId(ObjectId.toString(revCommit.getId()) + "/" + diff.getNewPath());
    return fileRevision;
  }

  protected String getContents(Repository repository, ObjectId id) throws MissingObjectException, IOException, UnsupportedEncodingException {
    if (id == null || ObjectId.zeroId().equals(id))
      return "";

    ObjectLoader loader = repository.open(id);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    loader.copyTo(out);
    byte[] byteArray = out.toByteArray();
    int length = ThothCoreUtil.charLength(byteArray);

    String body;
    if (length == -1)
      body = "<Binary contents>";
    else
      body = new String(byteArray, "UTF-8");
    return body;
  }

  protected CredentialsProvider getCredentialsProvider() {
    RepositoryDefinition repositoryDefinition = getContextDefinition().getRepositoryDefinition();
    String username = repositoryDefinition.getUsername();
    String password = repositoryDefinition.getPassword();
    return new UsernamePasswordCredentialsProvider(username, password);
  }

  protected Action translateAction(ChangeType changeType) {
    if (changeType == ChangeType.ADD)
      return Action.ADD;
    if (changeType == ChangeType.COPY)
      return Action.COPY;
    if (changeType == ChangeType.DELETE)
      return Action.DELETE;
    if (changeType == ChangeType.MODIFY)
      return Action.MODIFY;
    if (changeType == ChangeType.RENAME)
      return Action.RENAME;
    return Action.MODIFY;
  }

  protected void validateContextDefinition(ContextDefinition contextDefinition) throws ContentManagerException {
    RepositoryDefinition repositoryDefinition = contextDefinition.getRepositoryDefinition();
    if (StringUtils.isBlank(repositoryDefinition.getUsername()))
      throw new ContentManagerException("Username not set for repositiory " + repositoryDefinition.getName());
    if (StringUtils.isBlank(repositoryDefinition.getPassword()))
      throw new ContentManagerException("Password not set for repositiory " + repositoryDefinition.getName());
    if (StringUtils.isBlank(repositoryDefinition.getLocation()))
      throw new ContentManagerException("Location not set for repositiory " + repositoryDefinition.getName());
    if (StringUtils.isBlank(contextDefinition.getBranch()))
      throw new ContentManagerException("Branch not set for context " + contextDefinition.getName());
  }

  public String getContextFolder() throws ContextNotFoundException {
    Configuration config = getConfiguration();
    return config.getWorkspaceLocation() + getContextName() + "/";
  }

  protected void info(StringBuilder log, String message) {
    LOG.debug(message.trim());
    log.append(message + "\n");
  }

  protected void severe(StringBuilder log, String message) {
    LOG.error(message.trim());
    log.append("ERROR: " + message + "\n");
  }

  protected void severe(StringBuilder log, Exception e) {
    LOG.error(e.getMessage(), e);
    log.append("ERROR: " + e.getMessage() + "\n");
  }
}
