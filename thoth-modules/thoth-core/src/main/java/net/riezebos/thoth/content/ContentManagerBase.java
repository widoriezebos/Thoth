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
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.CacheManager;
import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ConfigurationFactory;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.content.search.Indexer;
import net.riezebos.thoth.content.search.SearchFactory;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.IncludeProcessor;
import net.riezebos.thoth.markdown.critics.CriticProcessingMode;
import net.riezebos.thoth.markdown.util.ProcessorError;
import net.riezebos.thoth.util.DiscardingList;
import net.riezebos.thoth.util.ThothUtil;

public abstract class ContentManagerBase implements ContentManager {
  private static final Logger LOG = LoggerFactory.getLogger(ContentManagerBase.class);
  protected static final String NO_CHANGES_DETECTED_MSG = "No changes detected";
  protected static final String CHANGES_DETECTED_MSG = "Changes detected, reindex requested";

  private String rootCanon = null;
  private boolean refreshing = false;
  private AutoRefresher autoRefresher = null;
  private Date latestRefresh = new Date();
  private ContextDefinition contextDefinition;

  public ContentManagerBase(ContextDefinition contextDefinition) {
    this.contextDefinition = contextDefinition;
  }

  public String getContext() {
    return getContextDefinition().getName();
  }

  public String getBranch() {
    return getContextDefinition().getBranch();
  }

  public ContextDefinition getContextDefinition() {
    return contextDefinition;
  }

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
    notifyContextContentsChanged();
  }

  protected void notifyContextContentsChanged() {
    CacheManager.expire(getContext());

    Thread indexerThread = new Thread() {
      public void run() {
        try {
          Indexer indexer = SearchFactory.getInstance().getIndexer(getContext());
          indexer.setIndexExtensions(ConfigurationFactory.getConfiguration().getIndexExtensions());
          indexer.index();
        } catch (ContentManagerException e) {
          LOG.error(e.getMessage(), e);
        }
      }
    };

    indexerThread.start();
    LOG.info("Contents updated. Launched indexer thread for context " + getContext());
  }

  @Override
  public MarkDownDocument getMarkDownDocument(String path, boolean suppressErrors, CriticProcessingMode criticProcessingMode)
      throws IOException, ContextNotFoundException {
    String documentPath = ThothUtil.normalSlashes(path);
    if (documentPath.startsWith("/"))
      documentPath = documentPath.substring(1);
    String physicalFilePath = getContextFolder() + documentPath;
    File file = new File(physicalFilePath);
    Configuration configuration = ConfigurationFactory.getConfiguration();

    IncludeProcessor processor = getIncludeProcessor(criticProcessingMode, physicalFilePath);

    try (FileInputStream in = new FileInputStream(file)) {
      String markdown = processor.execute(documentPath, in);
      if (processor.hasErrors() && (configuration.appendErrors() && !suppressErrors)) {
        markdown = appendErrors(processor, markdown);
      }
      MarkDownDocument markDownDocument = new MarkDownDocument(markdown, processor.getMetaTags(), processor.getErrors(), processor.getDocumentStructure());
      long latestMod = Math.max(file.lastModified(), processor.getLatestIncludeModificationDate());
      markDownDocument.setLastModified(new Date(latestMod));
      return markDownDocument;
    }
  }

  protected IncludeProcessor getIncludeProcessor(CriticProcessingMode criticProcessingMode, String physicalFilePath) throws ContextNotFoundException, IOException {
    Configuration configuration = ConfigurationFactory.getConfiguration();
    IncludeProcessor processor = new IncludeProcessor();
    processor.setLibrary(getContextFolder());
    processor.setRootFolder(ThothUtil.getFolder(physicalFilePath));
    processor.setCriticProcessingMode(criticProcessingMode);
    processor.setMaxNumberingLevel(configuration.getMaxHeaderNumberingLevel());
    return processor;
  }

  protected String appendErrors(IncludeProcessor processor, String markdown) {
    markdown += "\n\tThe following problems occurred during generation of this document:\n";
    for (ProcessorError error : processor.getErrors())
      markdown += "\t" + (error.getErrorMessage().replaceAll("\n", "\n\t").trim()) + "\n";
    return markdown;
  }

  public Date getLatestRefresh() {
    return latestRefresh;
  }

  protected void setLatestRefresh(Date date) {
    latestRefresh = date;
  }

  @Override
  public void enableAutoRefresh() {
    synchronized (this) {
      if (autoRefresher != null)
        autoRefresher.cancel();
      long autoRefreshIntervalMs = getContextDefinition().getRefreshIntervalMS();
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
  public List<Book> getBooks() throws ContextNotFoundException, IOException {
    String contextFolder = getContextFolder();
    File folder = new File(contextFolder);
    List<Book> result = new ArrayList<>();

    collectBooks(getConical(folder), folder, result, ConfigurationFactory.getConfiguration().getBookExtensions());
    Collections.sort(result);
    return result;
  }

  protected void collectBooks(String contextFolder, File folder, List<Book> result, List<String> bookExtensions) throws IOException, ContextNotFoundException {
    if (folder.isDirectory()) {
      for (File file : folder.listFiles()) {
        if (file.isFile()) {
          for (String ext : bookExtensions)
            if (file.getName().endsWith("." + ext)) {
              String absolutePath = file.getAbsolutePath();
              IncludeProcessor includeProcessor = getIncludeProcessor(CriticProcessingMode.DO_NOTHING, absolutePath);
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
  public String getContextFolder() throws ContextNotFoundException {
    Configuration config = ConfigurationFactory.getConfiguration();
    return config.getWorkspaceLocation() + getContext() + "/";
  }

  @Override
  public String getIndexFolder() throws ContextNotFoundException {
    Configuration config = ConfigurationFactory.getConfiguration();
    return config.getWorkspaceLocation() + getContext() + "-index/lucene/";
  }

  @Override
  public String getReverseIndexFileName() throws ContextNotFoundException {
    Configuration config = ConfigurationFactory.getConfiguration();
    return config.getWorkspaceLocation() + getContext() + "-index/reverseindex.bin";
  }

  @Override
  public String getReverseIndexIndirectFileName() throws ContextNotFoundException {
    Configuration config = ConfigurationFactory.getConfiguration();
    return config.getWorkspaceLocation() + getContext() + "-index/indirectreverseindex.bin";
  }

  @Override
  public String getErrorFileName() throws ContextNotFoundException {
    Configuration config = ConfigurationFactory.getConfiguration();
    return config.getWorkspaceLocation() + getContext() + "-index/errors.bin";
  }

  @Override
  public boolean accessAllowed(File file) throws IOException {
    String rootCanon = getRootCanonical();
    String canonicalPath = file.getCanonicalPath();
    return canonicalPath.startsWith(rootCanon);
  }

  protected String getRootCanonical() throws IOException {
    if (this.rootCanon == null) {
      File root = new File(ConfigurationFactory.getConfiguration().getWorkspaceLocation());
      String rootCanon = root.getCanonicalPath();
      this.rootCanon = rootCanon;
    }
    return this.rootCanon;
  }

  @Override
  public String getFileSystemPath(String path) throws ContextNotFoundException, IOException {
    if (path.startsWith("/"))
      path = path.substring(1);
    String absolutePath = getContextFolder() + path;
    if (!accessAllowed(new File(absolutePath)))
      return null;
    else
      return absolutePath;
  }

  @Override
  public List<ContentNode> list(String path) throws ContextNotFoundException, IOException {

    List<ContentNode> result = new ArrayList<>();

    String fileSystemPath = getFileSystemPath(path);
    String contextFolder = getContextFolder();

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
  public List<ContentNode> find(String fileSpec, boolean recursive) throws ContextNotFoundException, IOException {
    List<ContentNode> result = new ArrayList<>();

    String folderPart = ThothUtil.getFolder(fileSpec);
    String spec = ThothUtil.getFileName(fileSpec);
    if (fileSpec.indexOf('/') == -1)
      folderPart = "/";

    String folder = getFileSystemPath(folderPart);
    Path root = Paths.get(getContextFolder());

    Pattern pattern = Pattern.compile(ThothUtil.fileSpec2regExp(spec));
    traverseFolders(result, value -> pattern.matcher(ThothUtil.getFileName(value)).matches(), root, new File(folder), recursive);
    Collections.sort(result);
    return result;
  }

  protected long traverseFolders(List<ContentNode> result, Predicate<String> matcher, Path root, File currentFolder, boolean recursive) throws IOException {
    File[] folderContents = currentFolder.listFiles();
    long hash = 0;
    if (folderContents != null) {
      for (File file : folderContents) {
        if (file.isDirectory()) {
          if (recursive)
            hash += traverseFolders(result, matcher, root, file, recursive);
        } else {
          ContentNode node = createContentNode(file.getCanonicalPath(), root);
          if (matcher.test(node.getPath())) {
            result.add(node);
            hash += (file.getCanonicalPath().hashCode()) + file.lastModified();
          }
        }
      }
    }
    return hash;
  }

  @Override
  public List<ContentNode> getUnusedFragments() throws IOException, ContentManagerException {
    List<ContentNode> result = new ArrayList<>();

    Path root = Paths.get(getContextFolder());
    CacheManager cacheManager = CacheManager.getInstance(getContext());
    Map<String, List<String>> reverseIndex = cacheManager.getReverseIndex(false);
    traverseFolders(result, value -> isFragment(value) && !reverseIndex.containsKey(value), root, root.toFile(), true);
    Collections.sort(result);
    return result;
  }

  public boolean isFragment(String path) {
    return ConfigurationFactory.getConfiguration().isFragment(path);
  }

  protected ContentNode createContentNode(String fileSystemPath, Path contextPath) {
    Path filePath = Paths.get(fileSystemPath);
    Path relativePath = contextPath.relativize(filePath);
    File file = filePath.toFile();
    return new ContentNode("/" + relativePath.toString(), file);
  }

  @Override
  public long getContextChecksum() throws IOException, ContextNotFoundException {
    Path root = Paths.get(getContextFolder());
    Pattern pattern = Pattern.compile(ThothUtil.fileSpec2regExp("*.*"));
    return traverseFolders(new DiscardingList<ContentNode>(), value -> pattern.matcher(ThothUtil.getFileName(value)).matches(), root, root.toFile(), true);
  }
}
