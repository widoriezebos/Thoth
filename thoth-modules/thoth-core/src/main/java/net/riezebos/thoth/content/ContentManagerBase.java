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
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.CacheManager;
import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.search.Indexer;
import net.riezebos.thoth.content.search.SearchFactory;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.markdown.FileProcessor;
import net.riezebos.thoth.markdown.IncludeProcessor;
import net.riezebos.thoth.markdown.util.ProcessorError;
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

    if (isRefreshing())
      return "Refresh action is currently executing. Ignoring this request";

    try {
      markRefreshStart();
      return cloneOrPull();
    } finally {
      markFinishRefreshing();
    }
  }

  @Override
  public void reindex() {
    for (String context : getContexts()) {
      notifyContextContentsChanged(context);
    }
  }

  protected void notifyContextContentsChanged(final String context) {
    CacheManager.expire(context);

    Thread indexerThread = new Thread() {
      public void run() {
        try {
          Indexer indexer = SearchFactory.getInstance().getIndexer(context);
          indexer.setIndexExtensions(Configuration.getInstance().getIndexExtensions());
          indexer.index();
        } catch (ContentManagerException e) {
          LOG.error(e.getMessage(), e);
        }
      }
    };

    indexerThread.start();
    LOG.info("Contents updated. Launched indexer thread for context " + context);
  }

  @Override
  public MarkDownDocument getMarkDownDocument(String context, String path) throws IOException, ContextNotFoundException {
    String documentPath = ThothUtil.normalSlashes(path);
    if (documentPath.startsWith("/"))
      documentPath = documentPath.substring(1);
    String physicalFilePath = getContextFolder(context) + documentPath;
    File file = new File(physicalFilePath);
    IncludeProcessor processor = new IncludeProcessor();
    processor.setLibrary(getContextFolder(context));
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
  public Date getLatestRefresh(String context) {
    if (context == null)
      return getLatestRefresh();

    synchronized (latestRefresh) {
      return latestRefresh.get(context);
    }
  }

  protected void setLatestRefresh(String context, Date date) {
    synchronized (latestRefresh) {
      latestRefresh.put(context, date);
    }
  }

  @Override
  public void enableAutoRefresh() {
    synchronized (this) {
      if (autoRefresher != null)
        autoRefresher.cancel();
      long autoRefreshIntervalMs = Configuration.getInstance().getAutoRefreshIntervalMs();
      autoRefresher = autoRefreshIntervalMs <= 0 ? null : new AutoRefresher(autoRefreshIntervalMs, this);
    }
  }

  @Override
  public void disableAutoRefresh() {
    synchronized (this) {
      if (autoRefresher != null)
        autoRefresher.cancel();
      autoRefresher = null;
    }
  }

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
  public List<String> getContexts() {
    Configuration config = Configuration.getInstance();
    return config.getContexts();
  }

  @Override
  public List<Book> getBooks(String context) throws ContextNotFoundException, IOException {
    String contextFolder = getContextFolder(context);
    File folder = new File(contextFolder);
    List<Book> result = new ArrayList<>();

    collectBooks(getConical(folder), folder, result, Configuration.getInstance().getBookExtensions());
    Collections.sort(result);
    return result;
  }

  protected void collectBooks(String contextFolder, File folder, List<Book> result, List<String> bookExtensions) throws IOException {
    FileProcessor includeProcessor = new IncludeProcessor();
    if (folder.isDirectory()) {
      for (File file : folder.listFiles()) {
        if (file.isFile()) {
          for (String ext : bookExtensions)
            if (file.getName().endsWith("." + ext)) {
              String absolutePath = file.getAbsolutePath();
              File bookFile = new File(absolutePath);
              String canonicalPath = getConical(bookFile);
              if (canonicalPath.startsWith(contextFolder))
                canonicalPath = canonicalPath.substring(contextFolder.length());
              if (!canonicalPath.startsWith("/"))
                canonicalPath = "/" + canonicalPath;
              Book book = new Book(file.getName(), canonicalPath);
              book.setMetaTags(includeProcessor.getMetaTags(new FileInputStream(file)));
              result.add(book);
              break;
            }
        } else if (file.isDirectory())
          collectBooks(contextFolder, file, result, bookExtensions);
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
  public String getContextFolder(String context) throws ContextNotFoundException {
    validateContext(context);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + context + "/";
  }

  @Override
  public String getIndexFolder(String context) throws ContextNotFoundException {
    validateContext(context);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + context + "-index/lucene/";
  }

  @Override
  public String getReverseIndexFileName(String context) throws ContextNotFoundException {
    validateContext(context);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + context + "-index/reverseindex.bin";
  }

  @Override
  public String getReverseIndexIndirectFileName(String context) throws ContextNotFoundException {
    validateContext(context);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + context + "-index/indirectreverseindex.bin";
  }

  @Override
  public String getErrorFileName(String context) throws ContextNotFoundException {
    validateContext(context);
    Configuration config = Configuration.getInstance();
    return config.getWorkspaceLocation() + context + "-index/errors.bin";
  }

  protected void validateContext(String context) throws ContextNotFoundException {
    if (!getContexts().contains(context))
      throw new ContextNotFoundException(context);
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
  public String getFileSystemPath(String context, String path) throws ContextNotFoundException, IOException {
    if (path.startsWith("/"))
      path = path.substring(1);
    String absolutePath = getContextFolder(context) + path;
    if (!accessAllowed(new File(absolutePath)))
      return null;
    else
      return absolutePath;
  }

  @Override
  public List<ContentNode> list(String context, String path) throws ContextNotFoundException, IOException {

    List<ContentNode> result = new ArrayList<>();

    String fileSystemPath = getFileSystemPath(context, path);
    String contextFolder = getContextFolder(context);

    Path contextPath = Paths.get(contextFolder);

    File file = new File(fileSystemPath);
    if (file.isFile()) {
      result.add(createContentNode(fileSystemPath, contextPath));
    } else {
      for (File child : file.listFiles()) {
        if (!child.getName().startsWith("."))
          result.add(createContentNode(child.getAbsolutePath(), contextPath));
      }
    }
    Collections.sort(result);
    return result;
  }

  @Override
  public List<ContentNode> find(String context, String fileSpec, boolean recursive) throws ContextNotFoundException, IOException {
    List<ContentNode> result = new ArrayList<>();

    String folderPart = ThothUtil.getFolder(fileSpec);
    String spec = ThothUtil.getFileName(fileSpec);
    if (fileSpec.indexOf('/') == -1)
      folderPart = "/";

    String folder = getFileSystemPath(context, folderPart);
    Path root = Paths.get(getContextFolder(context));

    Pattern pattern = Pattern.compile(ThothUtil.fileSpec2regExp(spec));
    traverseFolders(result, value -> pattern.matcher(ThothUtil.getFileName(value)).matches(), root, new File(folder), recursive);
    Collections.sort(result);
    return result;
  }

  protected void traverseFolders(List<ContentNode> result, Predicate<String> matcher, Path root, File currentFolder, boolean recursive) throws IOException {
    for (File file : currentFolder.listFiles()) {
      if (file.isDirectory()) {
        if (recursive)
          traverseFolders(result, matcher, root, file, recursive);
      } else {
        ContentNode node = createContentNode(file.getCanonicalPath(), root);
        if (matcher.test(node.getPath()))
          result.add(node);
      }
    }
  }

  @Override
  public List<ContentNode> getUnusedFragments(String context) throws IOException, ContentManagerException {
    List<ContentNode> result = new ArrayList<>();

    Path root = Paths.get(getContextFolder(context));
    CacheManager cacheManager = CacheManager.getInstance(context);
    Map<String, List<String>> reverseIndex = cacheManager.getReverseIndex(false);
    traverseFolders(result, value -> isFragment(context, value) && !reverseIndex.containsKey(value), root, root.toFile(), true);
    Collections.sort(result);
    return result;
  }

  public boolean isFragment(String context, String path) {
    return Configuration.getInstance().isFragment(path);
  }

  protected ContentNode createContentNode(String fileSystemPath, Path contextPath) {
    Path filePath = Paths.get(fileSystemPath);
    Path relativePath = contextPath.relativize(filePath);
    File file = filePath.toFile();
    return new ContentNode("/" + relativePath.toString(), file);
  }

}
