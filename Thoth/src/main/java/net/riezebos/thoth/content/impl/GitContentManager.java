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

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.content.ContentManagerBase;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.Revision;
import net.riezebos.thoth.content.versioncontrol.Revision.Action;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.PagedList;

public class GitContentManager extends ContentManagerBase {

  private static final Logger LOG = LoggerFactory.getLogger(GitContentManager.class);
  private static final String HEAD_TREE = "HEAD^{tree}";

  public GitContentManager() throws ContentManagerException {
  }

  protected synchronized String cloneOrPull() throws ContentManagerException {
    StringBuilder log = new StringBuilder();
    try {
      String message = "Pulling git repositories for changes";
      info(log, message);
      LOG.info(message);
      Configuration config = Configuration.getInstance();
      String repositoryUrl = config.getValue("git.repository");
      String workspaceLocation = config.getWorkspaceLocation();
      if (StringUtils.isBlank(workspaceLocation))
        throw new IllegalArgumentException("No environment variable or system variable named " + Configuration.WORKSPACELOCATION + " was set");
      File file = new File(workspaceLocation);
      if (!file.isDirectory()) {
        severe(log, "The library path " + workspaceLocation + " does not exist. Cannot initialize content manager");
      } else {
        for (String branch : config.getBranches()) {
          try {
            String branchFolder = getBranchFolder(branch);
            File target = new File(branchFolder);
            CredentialsProvider credentialsProvider = getCredentialsProvider();

            if (target.isDirectory()) {
              info(log, "\nRepository for branch " + branch + " found at " + branchFolder);
              try (Git repos = getRepository(branch)) {

                Repository repository = repos.getRepository();
                ObjectId oldHead = repository.resolve(HEAD_TREE);

                PullResult pullResult = repos.pull().setCredentialsProvider(credentialsProvider).call();
                info(log, pullResult.isSuccessful() ? "Pull was successful" : "Pull failed");
                ObjectId newHead = repository.resolve(HEAD_TREE);

                if ((oldHead == null && newHead != null)//
                    || (oldHead != null && !oldHead.equals(newHead)))
                  notifyBranchContentsChanged(branch);

                setLatestRefresh(branch, new Date());
              } catch (Exception e) {
                severe(log, e);
              }
            } else {
              info(log, "Cloning from " + repositoryUrl + " to " + branchFolder);
              target.mkdirs();
              try (Git result = Git.cloneRepository()//
                  .setURI(repositoryUrl)//
                  .setBranch(branch)//
                  .setCredentialsProvider(credentialsProvider)//
                  .setDirectory(target).call()) {
                info(log, "Cloned repository: " + result.getRepository().getDirectory());
                setLatestRefresh(branch, new Date());
                notifyBranchContentsChanged(branch);
              } catch (Exception e) {
                severe(log, e);
              }
            }
          } catch (Exception e) {
            severe(log, e);
          }
        }
      }
      info(log, "Git refresh completed");

    } catch (Exception e) {
      throw new ContentManagerException(e);
    }

    return log.toString();
  }

  protected Git getRepository(String branch) throws BranchNotFoundException, IOException {
    String branchFolder = getBranchFolder(branch);
    File target = new File(branchFolder);
    return Git.open(target);
  }

  public PagedList<Commit> getCommits(String branch, String path, int pageNumber, int pageSize) throws ContentManagerException {
    path = ensureRelative(path);
    try (Git repos = getRepository(branch)) {
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

  protected String ensureRelative(String path) {
    if (path == null)
      return null;
    if (path.startsWith("/"))
      path = path.substring(1);
    return path;
  }

  protected Commit getCommit(Repository repository, RevCommit revCommit, String path)
      throws MissingObjectException, IncorrectObjectTypeException, IOException, UnsupportedEncodingException {
    path = ensureRelative(path);

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
            fileRevision.setCommitMessage(revCommit.getFullMessage());
            fileRevision.setCommitId(ObjectId.toString(revCommit.getId()) + "/" + path);
            commit.addRevision(fileRevision);
          }
        }
      }
    }
    return commit;
  }

  public SourceDiff getDiff(String branch, String diffSpec) throws ContentManagerException {

    int idx = diffSpec.indexOf('/');
    if (idx == -1)
      return null;

    String id = diffSpec.substring(0, idx);
    String path = diffSpec.substring(idx + 1);

    if (id == null || ObjectId.zeroId().equals(ObjectId.fromString(id)))
      return null;

    SourceDiff result = null;

    try (Git git = getRepository(branch); RevWalk revWalk = new RevWalk(git.getRepository()); SimpleDiffFormatter df = new SimpleDiffFormatter()) {
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
    fileRevision.setCommitMessage(revCommit.getFullMessage());
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
    int length = charLength(byteArray);

    String body;
    if (length == -1)
      body = "<Binary contents>";
    else
      body = new String(byteArray, "UTF-8");
    return body;
  }

  /**
   * Returns the number of UTF-8 characters, or -1 if the array does not contain a valid UTF-8 string. Overlong encodings, null characters, invalid Unicode
   * values, and surrogates are accepted.
   */
  public int charLength(byte[] bytes) {
    int charCount = 0, expectedLen;

    for (int i = 0; i < bytes.length; i++) {
      charCount++;
      // Lead byte analysis
      if ((bytes[i] & 0b10000000) == 0b00000000)
        continue;
      else if ((bytes[i] & 0b11100000) == 0b11000000)
        expectedLen = 2;
      else if ((bytes[i] & 0b11110000) == 0b11100000)
        expectedLen = 3;
      else if ((bytes[i] & 0b11111000) == 0b11110000)
        expectedLen = 4;
      else if ((bytes[i] & 0b11111100) == 0b11111000)
        expectedLen = 5;
      else if ((bytes[i] & 0b11111110) == 0b11111100)
        expectedLen = 6;
      else
        return -1;

      // Count trailing bytes
      while (--expectedLen > 0) {
        if (++i >= bytes.length) {
          return -1;
        }
        if ((bytes[i] & 0b11000000) != 0b10000000) {
          return -1;
        }
      }
    }
    return charCount;
  }

  protected CredentialsProvider getCredentialsProvider() {
    Configuration config = Configuration.getInstance();
    String username = config.getValue("git.username");
    String password = config.getValue("git.password");
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
