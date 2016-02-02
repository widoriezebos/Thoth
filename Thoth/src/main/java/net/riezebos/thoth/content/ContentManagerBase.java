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
package net.riezebos.thoth.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.CacheManager;
import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.beans.BookClassification;
import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.markdown.FileProcessor;
import net.riezebos.thoth.content.markdown.IncludeProcessor;
import net.riezebos.thoth.content.markdown.util.ProcessorError;
import net.riezebos.thoth.content.search.Indexer;
import net.riezebos.thoth.content.search.SearchFactory;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.ThothUtil;

public abstract class ContentManagerBase implements ContentManager {
  private static final Logger LOG = LoggerFactory.getLogger(ContentManagerBase.class);

  private String rootCanon = null;
  private boolean refreshing = false;
  private AutoRefresher autoRefresher = null;
  private Map<String, Date> latestRefresh = new HashMap<>();

  protected abstract String cloneOrPull() throws ContentManagerException;

  @Override
  public synchronized String refresh() throws ContentManagerException {
    try {
      markRefreshStart();
      return cloneOrPull();
    } finally {
      markFinishRefreshing();
    }
  }

  @Override
  public void reindex() {
    for (String branch : getBranches()) {
      notifyBranchContentsChanged(branch);
    }
  }

  protected void notifyBranchContentsChanged(final String branch) {
    CacheManager.expire(branch);

    Thread indexerThread = new Thread() {
      public void run() {
        try {
          Indexer indexer = SearchFactory.getInstance().getIndexer(branch);
          indexer.setIndexExtensions(Configuration.getInstance().getIndexExtensions());
          indexer.index();
        } catch (ContentManagerException e) {
          LOG.error(e.getMessage(), e);
        }
      }
    };

    indexerThread.start();
    LOG.info("Launched indexer thread for branch " + branch);
  }

  @Override
  public MarkDownDocument getMarkDownDocument(String branch, String path) throws IOException, BranchNotFoundException {
    String documentPath = ThothUtil.normalSlashes(path);
    if (documentPath.startsWith("/"))
      documentPath = documentPath.substring(1);
    String physicalFilePath = getBranchFolder(branch) + documentPath;
    File file = new File(physicalFilePath);
    IncludeProcessor processor = new IncludeProcessor();
    processor.setLibrary(getBranchFolder(branch));
    processor.setRootFolder(ThothUtil.getFolder(physicalFilePath));

    try (FileInputStream in = new FileInputStream(file)) {
      String markdown = processor.execute(documentPath, in);
      if (processor.hasErrors() && Configuration.getInstance().appendErrors()) {
        markdown = appendErrors(processor, markdown);
      }
      MarkDownDocument markDownDocument = new MarkDownDocument(markdown, processor.getMetaTags(), processor.getErrors(), processor.getDocumentStructure());
      return markDownDocument;
    }
  }

  protected String appendErrors(IncludeProcessor processor, String markdown) {
    markdown += "\n\tThe following problems occurred during generation of this document:\n";
    for (ProcessorError error : processor.getErrors())
      markdown += "\t" + (error.getErrorMessage().replaceAll("\n", "\n\t").trim()) + "\n";
    return markdown;
  }

  @Override
  public Date getLatestRefresh() {
    Date result = null;
    synchronized (latestRefresh) {
      for (Date date : latestRefresh.values()) {
        if (result == null || date.compareTo(result) < 0)
          result = date;
      }
    }
    return result;
  }

  @Override
  public Date getLatestRefresh(String branch) {
    if (branch == null)
      return getLatestRefresh();

    synchronized (latestRefresh) {
      return latestRefresh.get(branch);
    }
  }

  protected void setLatestRefresh(String branch, Date date) {
    synchronized (latestRefresh) {
      latestRefresh.put(branch, date);
    }
  }

  @Override
  public void enableAutoRefresh() {
    if (autoRefresher != null)
      autoRefresher.cancel();
    long autoRefreshIntervalMs = Configuration.getInstance().getAutoRefreshIntervalMs();
    autoRefresher = autoRefreshIntervalMs <= 0 ? null : new AutoRefresher(autoRefreshIntervalMs, this);
  }

  @Override
  public boolean isRefreshing() {
    return this.refreshing;
  }

  protected void markFinishRefreshing() {
    refreshing = false;
  }

  protected void markRefreshStart() {
    refreshing = true;
  }

  @Override
  public List<String> getBranches() {
    Configuration config = Configuration.getInstance();
    return config.getBranches();
  }

  @Override
  public List<Book> getBooks(String branch) throws BranchNotFoundException, IOException {
    String branchFolder = getBranchFolder(branch);
    File folder = new File(branchFolder);
    List<Book> result = new ArrayList<>();

    collectBooks(getConical(folder), folder, result, Configuration.getInstance().getBookExtensions());
    Collections.sort(result);
    return result;
  }

  protected void collectBooks(String branchFolder, File folder, List<Book> result, List<String> bookExtensions) throws IOException {
    FileProcessor includeProcessor = new IncludeProcessor();
    if (folder.isDirectory()) {
      for (File file : folder.listFiles()) {
        if (file.isFile()) {
          for (String ext : bookExtensions)
            if (file.getName().endsWith("." + ext)) {
              String absolutePath = file.getAbsolutePath();
              File bookFile = new File(absolutePath);
              String canonicalPath = getConical(bookFile);
              if (canonicalPath.startsWith(branchFolder))
                canonicalPath = canonicalPath.substring(branchFolder.length());
              if (!canonicalPath.startsWith("/"))
                canonicalPath = "/" + canonicalPath;
              Book book = new Book(file.getName(), canonicalPath);
              book.setMetaTags(includeProcessor.getMetaTags(new FileInputStream(file)));
              result.add(book);
              break;
            }
        } else if (file.isDirectory())
          collectBooks(branchFolder, file, result, bookExtensions);
      }
    }
  }

  protected String getConical(File bookFile) {
    try {
      return ThothUtil.normalSlashes(bookFile.getCanonicalPath());
    } catch (Exception e) {
      LOG.error(e.getMessage() + " for " + bookFile.getAbsolutePath());
      return bookFile.getAbsolutePath();
    }
  }

  @Override
  public String getBranchFolder(String branch) throws BranchNotFoundException {
    validateBranch(branch);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + branch + "/";
  }

  @Override
  public String getIndexFolder(String branch) throws BranchNotFoundException {
    validateBranch(branch);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + branch + "-index/lucene/";
  }

  @Override
  public String getReverseIndexFileName(String branch) throws BranchNotFoundException {
    validateBranch(branch);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + branch + "-index/reverseindex.bin";
  }

  @Override
  public String getReverseIndexIndirectFileName(String branch) throws BranchNotFoundException {
    validateBranch(branch);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + branch + "-index/indirectreverseindex.bin";
  }

  @Override
  public String getErrorFileName(String branch) throws BranchNotFoundException {
    validateBranch(branch);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + branch + "-index/errors.bin";
  }

  protected void validateBranch(String branch) throws BranchNotFoundException {
    if (!getBranches().contains(branch))
      throw new BranchNotFoundException(branch);
  }

  @Override
  public boolean accessAllowed(File file) throws IOException {
    String rootCanon = getRootCanonical();
    String canonicalPath = file.getCanonicalPath();
    return canonicalPath.startsWith(rootCanon);
  }

  protected String getRootCanonical() throws IOException {
    if (this.rootCanon == null) {
      File root = new File(Configuration.getInstance().getWorkspaceLocation());
      String rootCanon = root.getCanonicalPath();
      this.rootCanon = rootCanon;
    }
    return this.rootCanon;
  }

  @Override
  public List<BookClassification> getClassification(List<Book> books, String metaTagName, String defaultValue) {
    Map<String, BookClassification> classificationMap = new HashMap<>();

    for (Book book : books) {

      String classificationSpec = book.getMetaTag(metaTagName);

      if (classificationSpec == null) {
        if ("folder".equalsIgnoreCase(metaTagName))
          classificationSpec = book.getFolder();
        else
          classificationSpec = defaultValue;
      }

      for (String classificationName : ThothUtil.tokenize(classificationSpec)) {
        BookClassification classification = classificationMap.get(classificationName);
        if (classification == null) {
          classification = new BookClassification(classificationName);
          classificationMap.put(classificationName, classification);
        }
        classification.getBooks().add(book);
      }
    }

    for (BookClassification classification : classificationMap.values())
      classification.sortBooks();

    List<BookClassification> result = new ArrayList<>();
    result.addAll(classificationMap.values());
    Collections.sort(result);
    return result;
  }

  @Override
  public String getFileSystemPath(String branch, String path) throws BranchNotFoundException, IOException {
    if (path.startsWith("/"))
      path = path.substring(1);
    String absolutePath = getBranchFolder(branch) + path;
    if (!accessAllowed(new File(absolutePath)))
      return null;
    else
      return absolutePath;
  }

  @Override
  public List<ContentNode> list(String branch, String path) throws BranchNotFoundException, IOException {

    List<ContentNode> result = new ArrayList<>();

    String fileSystemPath = getFileSystemPath(branch, path);
    String branchFolder = getBranchFolder(branch);

    Path branchPath = Paths.get(branchFolder);

    File file = new File(fileSystemPath);
    if (file.isFile()) {
      result.add(createContentNode(fileSystemPath, branchPath));
    } else {
      for (File child : file.listFiles()) {
        if (!child.getName().startsWith("."))
          result.add(createContentNode(child.getAbsolutePath(), branchPath));
      }
    }
    Collections.sort(result);
    return result;
  }

  protected ContentNode createContentNode(String fileSystemPath, Path branchPath) {
    Path filePath = Paths.get(fileSystemPath);
    Path relativePath = branchPath.relativize(filePath);
    File file = filePath.toFile();
    return new ContentNode("/" + relativePath.toString(), file);
  }

  @Override
  public List<ContentNode> find(String branch, String fileSpec, boolean recursive) throws BranchNotFoundException, IOException {
    List<ContentNode> result = new ArrayList<>();

    String folderPart = ThothUtil.getFolder(fileSpec);
    String spec = ThothUtil.getFileName(fileSpec);
    if (fileSpec.indexOf('/') == -1)
      folderPart = "/";

    String folder = getFileSystemPath(branch, folderPart);
    Path root = Paths.get(getBranchFolder(branch));

    Pattern pattern = Pattern.compile(ThothUtil.fileSpec2regExp(spec));
    traverseFolders(result, pattern, root, new File(folder), recursive);
    Collections.sort(result);
    return result;
  }

  protected void traverseFolders(List<ContentNode> result, Pattern pattern, Path root, File currentFolder, boolean recursive) throws IOException {
    for (File file : currentFolder.listFiles()) {
      if (file.isDirectory()) {
        if (recursive)
          traverseFolders(result, pattern, root, file, recursive);
      } else if (pattern.matcher(file.getName()).matches()) {
        result.add(createContentNode(file.getCanonicalPath(), root));
      }
    }
  }
}
