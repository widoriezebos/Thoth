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

import java.io.IOException;
import java.io.InputStream;
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
import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.markdown.IncludeProcessor;
import net.riezebos.thoth.markdown.critics.CriticProcessingMode;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.markdown.filehandle.FileSystem;
import net.riezebos.thoth.markdown.util.DocumentNode;
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
  private SkinManager skinManager;
  private FileSystem fileSystem;

  public ContentManagerBase(ContextDefinition contextDefinition) {
    this.contextDefinition = contextDefinition;
  }

  @Override
  public InputStream getInputStream(String path) throws IOException {
    try {
      String resourcePath = ThothUtil.stripPrefix(path, "/");
      InputStream inputStream = null;
      // First check whether the file exists; because then we are done.
      FileHandle fileHandle = getFileHandle(resourcePath);
      if (!fileHandle.isFile()) {
        // Not found; then check for any inheritance of skin related paths.
        // Complication is that we might move from the library into the classpath so we need
        // to handle that as well here
        SkinManager skinManager = getSkinManager();
        String inheritedPath = skinManager.getInheritedPath(path);

        while (inheritedPath != null && inputStream == null && !fileHandle.isFile()) {
          resourcePath = inheritedPath;
          // Moving into classpath now?
          if (resourcePath.startsWith(Configuration.CLASSPATH_PREFIX)) {
            String resourceName = resourcePath.substring(Configuration.CLASSPATH_PREFIX.length());
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
          } else {
            // Ok not moved into the classpath. We have to check for the inherited file now:
            if (resourcePath != null)
              fileHandle = getFileHandle(resourcePath);
          }
          // Do we need to move up the hierarchy still?
          inheritedPath = skinManager.getInheritedPath(inheritedPath);
        }
      }

      // If the inputstream is set now; it came from the classpath.
      // If it is not set; then it will have to come from the file now; if it exists
      if (inputStream == null && fileHandle.isFile())
        inputStream = fileHandle.getInputStream();
      return inputStream;
    } catch (ContentManagerException e) {
      throw new IOException(e);
    }
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
  public SkinManager getSkinManager() throws SkinManagerException {
    if (skinManager == null)
      skinManager = new SkinManager(this, ConfigurationFactory.getConfiguration().getDefaultSkin());
    return skinManager;
  }

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
    skinManager = null;

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
    FileHandle file = getFileHandle(documentPath);
    Configuration configuration = ConfigurationFactory.getConfiguration();

    IncludeProcessor processor = getIncludeProcessor(criticProcessingMode, documentPath);

    try (InputStream in = file.getInputStream()) {
      String markdown = processor.execute(documentPath, in);
      if (processor.hasErrors() && (configuration.appendErrors() && !suppressErrors)) {
        markdown = appendErrors(processor, markdown);
      }
      Map<String, String> metaTags = processor.getMetaTags();
      List<ProcessorError> errors = processor.getErrors();
      DocumentNode documentStructure = processor.getDocumentStructure();
      MarkDownDocument markDownDocument = new MarkDownDocument(markdown, metaTags, errors, documentStructure);
      long latestMod = Math.max(file.lastModified(), processor.getLatestIncludeModificationDate());
      markDownDocument.setLastModified(new Date(latestMod));
      return markDownDocument;
    }
  }

  protected IncludeProcessor getIncludeProcessor(CriticProcessingMode criticProcessingMode, String documentPath) throws ContextNotFoundException, IOException {
    Configuration configuration = ConfigurationFactory.getConfiguration();
    IncludeProcessor processor = new IncludeProcessor();
    processor.setFileSystem(getFileSystem());
    processor.setLibrary("");
    processor.setRootFolder(ThothUtil.getFolder(documentPath));
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
    FileHandle folder = getFileHandle("/");
    List<Book> result = new ArrayList<>();

    collectBooks(folder.getCanonicalPath(), folder, result, ConfigurationFactory.getConfiguration().getBookExtensions());
    Collections.sort(result);
    return result;
  }

  protected void collectBooks(String contextFolder, FileHandle folder, List<Book> result, List<String> bookExtensions)
      throws IOException, ContextNotFoundException {
    if (folder.isDirectory()) {
      for (FileHandle file : folder.listFiles()) {
        if (file.isFile()) {
          for (String ext : bookExtensions)
            if (file.getName().endsWith("." + ext)) {
              String absolutePath = file.getAbsolutePath();
              IncludeProcessor includeProcessor = getIncludeProcessor(CriticProcessingMode.DO_NOTHING, absolutePath);
              FileHandle bookFile = getFileHandle(absolutePath);
              String canonicalPath = bookFile.getCanonicalPath();
              if (canonicalPath.startsWith(contextFolder))
                canonicalPath = canonicalPath.substring(contextFolder.length());
              if (!canonicalPath.startsWith("/"))
                canonicalPath = "/" + canonicalPath;
              Book book = new Book(file.getName(), canonicalPath);
              book.setMetaTags(includeProcessor.getMetaTags(file.getInputStream()));
              result.add(book);
              break;
            }
        } else if (file.isDirectory())
          collectBooks(contextFolder, file, result, bookExtensions);
      }
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
  public boolean accessAllowed(FileHandle file) throws IOException {
    String rootCanon = getRootCanonical();
    String canonicalPath = file.getCanonicalPath();
    return canonicalPath.startsWith(rootCanon);
  }

  protected String getRootCanonical() throws IOException {
    if (this.rootCanon == null) {
      FileHandle root = getFileHandle(ConfigurationFactory.getConfiguration().getWorkspaceLocation());
      this.rootCanon = root.getCanonicalPath();
    }
    return this.rootCanon;
  }

  @Override
  public List<ContentNode> list(String path) throws ContextNotFoundException, IOException {

    List<ContentNode> result = new ArrayList<>();

    FileHandle fileHandle = getFileHandle(path);
    if (fileHandle.isFile()) {
      result.add(new ContentNode(fileHandle.getCanonicalPath(), fileHandle));
    } else {
      for (FileHandle child : fileHandle.listFiles()) {
        if (!child.getName().startsWith("."))
          result.add(new ContentNode(child.getCanonicalPath(), child));
      }
    }
    Collections.sort(result);
    return result;
  }

  @Override
  public List<ContentNode> find(String fileSpec, boolean recursive) throws ContextNotFoundException, IOException {
    List<ContentNode> result = new ArrayList<>();

    String folder = ThothUtil.getFolder(fileSpec);
    String spec = ThothUtil.getFileName(fileSpec);
    if (fileSpec.indexOf('/') == -1)
      folder = "/";

    Pattern pattern = Pattern.compile(ThothUtil.fileSpec2regExp(spec));
    traverseFolders(result, value -> pattern.matcher(ThothUtil.getFileName(value)).matches(), getFileHandle(folder), recursive);
    Collections.sort(result);
    return result;
  }

  protected long traverseFolders(List<ContentNode> result, Predicate<String> matcher, FileHandle currentFolder, boolean recursive) throws IOException {
    FileHandle[] folderContents = currentFolder.listFiles();
    long hash = 0;
    if (folderContents != null) {
      for (FileHandle file : folderContents) {
        if (file.isDirectory()) {
          if (recursive)
            hash += traverseFolders(result, matcher, file, recursive);
        } else {
          if (matcher.test(file.getAbsolutePath())) {
            result.add(new ContentNode(file.getAbsolutePath(), file));
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

    Path root = Paths.get("/");
    CacheManager cacheManager = CacheManager.getInstance(getContext());
    Map<String, List<String>> reverseIndex = cacheManager.getReverseIndex(false);
    traverseFolders(result, value -> isFragment(value) && !reverseIndex.containsKey(value), getFileHandle(root.toString()), true);
    Collections.sort(result);
    return result;
  }

  public boolean isFragment(String path) {
    return ConfigurationFactory.getConfiguration().isFragment(path);
  }

  @Override
  public long getContextChecksum() throws IOException, ContextNotFoundException {
    Pattern pattern = Pattern.compile(ThothUtil.fileSpec2regExp("*.*"));
    return traverseFolders(new DiscardingList<ContentNode>(), value -> pattern.matcher(ThothUtil.getFileName(value)).matches(), getFileHandle("/"), true);
  }

  protected FileSystem getFileSystem() {
    return fileSystem;
  }

  public void setFileSystem(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  public FileHandle getFileHandle(String filePath) {
    return fileSystem.getFileHandle(filePath);
  }

}
