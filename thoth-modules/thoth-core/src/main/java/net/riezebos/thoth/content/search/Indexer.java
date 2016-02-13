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
package net.riezebos.thoth.content.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.CacheManager;
import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.ConfigurationFactory;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.IndexerException;
import net.riezebos.thoth.markdown.critics.CriticProcessingMode;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.markdown.util.DocumentNode;
import net.riezebos.thoth.markdown.util.ProcessorError;
import net.riezebos.thoth.util.ThothUtil;

public class Indexer {
  public static final String INDEX_CONTENTS = "contents";
  public static final String INDEX_TYPE = "type";
  public static final String INDEX_TITLE = "title";
  public static final String INDEX_PATH = "path";
  public static final String INDEX_USED = "used";
  public static final String INDEX_EXTENSION = "ext";
  public static final String INDEX_MODIFIED = "modified";
  public static final String TYPE_DOCUMENT = "document";
  public static final String TYPE_OTHER = "other";

  private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

  private String indexFolder;
  private FileHandle contextFolder;
  private boolean recreate = true;
  private ContentManager contentManager;
  private Set<String> extensions = new HashSet<>();
  private static Set<String> activeIndexers = new HashSet<>();

  public static void main(String[] args) throws ContentManagerException {
    Indexer indexer = SearchFactory.getInstance().getIndexer("Erasmus");
    indexer.index();
    System.out.println("Done");
  }

  protected Indexer(ContentManager contentManager) throws ContextNotFoundException, ContentManagerException {
    this.contentManager = contentManager;
    this.indexFolder = contentManager.getIndexFolder();
    this.contextFolder = contentManager.getFileHandle("/");
    this.setIndexExtensions(ConfigurationFactory.getConfiguration().getIndexExtensions());
  }

  public void index() throws ContentManagerException {

    String context = contentManager.getContext();
    synchronized (activeIndexers) {
      if (activeIndexers.contains(context)) {
        LOG.warn("Indexer for context " + context + " is already (still?) active. Not starting a new index operation");
        return;
      }
      activeIndexers.add(context);
    }

    try {
      Date start = new Date();
      LOG.info("Indexing " + context + " to directory '" + indexFolder + "'...");

      IndexWriter writer = getWriter(recreate);
      IndexingContext indexingContext = new IndexingContext();
      indexDirectory(writer, contextFolder, indexingContext);

      sortIndexLists(indexingContext.getIndirectReverseIndex());
      sortIndexLists(indexingContext.getDirectReverseIndex());
      Collections.sort(indexingContext.getErrors());

      cacheResults(indexingContext);

      // NOTE: if you want to maximize search performance,
      // you can optionally call forceMerge here. This can be
      // a terribly costly operation, so generally it's only
      // worth it when your index is relatively static (ie
      // you're done adding documents to it):
      //
      writer.forceMerge(1);

      writer.close();

      markUnusedDocuments(indexingContext.getDirectReverseIndex());

      Date end = new Date();
      LOG.info("Indexing context " + context + " took " + (end.getTime() - start.getTime()) + " milliseconds");
    } catch (IOException e) {
      throw new ContentManagerException(e);
    } finally {
      synchronized (activeIndexers) {
        activeIndexers.remove(context);
      }
    }
  }

  protected IndexWriter getWriter(boolean wipeIndex) throws IOException {
    Directory dir = FSDirectory.open(Paths.get(indexFolder));
    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

    if (wipeIndex) {
      iwc.setOpenMode(OpenMode.CREATE);
    } else {
      iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
    }

    IndexWriter writer = new IndexWriter(dir, iwc);
    return writer;
  }

  protected void markUnusedDocuments(Map<String, List<String>> directReverseIndex) throws IOException, ContentManagerException {

    String indexFolder = contentManager.getIndexFolder();

    try (IndexWriter writer = getWriter(false); IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexFolder)))) {
      IndexSearcher searcher = new IndexSearcher(reader);
      for (ContentNode node : contentManager.getUnusedFragments()) {
        TermQuery query = new TermQuery(new Term(Indexer.INDEX_PATH, node.getPath()));

        TopDocs results = searcher.search(query, 10, Sort.RELEVANCE);
        ScoreDoc[] hits = results.scoreDocs;

        for (ScoreDoc scoreDoc : hits) {
          Document document = searcher.doc(scoreDoc.doc);
          document.add(new TextField(INDEX_USED, "false", Store.YES));
          writer.updateDocument(new Term(INDEX_PATH, node.getPath()), document);
        }
      }
    }
  }

  protected void cacheResults(IndexingContext indexingContext) throws ContextNotFoundException, IOException, FileNotFoundException {
    String reverseIndexFile = contentManager.getReverseIndexFileName();
    String indirectReverseIndexFile = contentManager.getReverseIndexIndirectFileName();
    String errorFile = contentManager.getErrorFileName();

    synchronized (CacheManager.getFileLock()) {
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(reverseIndexFile)))) {
        oos.writeObject(indexingContext.getDirectReverseIndex());
      }
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(indirectReverseIndexFile)))) {
        oos.writeObject(indexingContext.getIndirectReverseIndex());
      }
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(errorFile)))) {
        oos.writeObject(indexingContext.getErrors());
      }
    }

    CacheManager cacheManager = CacheManager.getInstance(contentManager.getContext());
    cacheManager.cacheReverseIndex(true, indexingContext.getIndirectReverseIndex());
    cacheManager.cacheReverseIndex(false, indexingContext.getDirectReverseIndex());
    cacheManager.cacheErrors(indexingContext.getErrors());
  }

  protected void sortIndexLists(Map<String, List<String>> map) {
    for (Entry<String, List<String>> entry : map.entrySet())
      Collections.sort(entry.getValue());
  }

  protected void indexDirectory(IndexWriter writer, FileHandle fileHandle, IndexingContext context) throws IOException, ContextNotFoundException {

    if (fileHandle.isDirectory()) {
      for (FileHandle children : fileHandle.listFiles()) {
        if (children.isFile())
          indexFile(writer, children, context);
        else
          indexDirectory(writer, children, context);
      }
    } else {
      indexFile(writer, fileHandle, context);
    }
  }

  protected void indexFile(IndexWriter writer, FileHandle fileHandle, IndexingContext indexingContext) throws IOException, ContextNotFoundException {

    if (!ignore(fileHandle.getAbsolutePath())) {

      try {
        String resourcePath = fileHandle.getAbsolutePath();
        MarkDownDocument markDownDocument = contentManager.getMarkDownDocument(resourcePath, true, CriticProcessingMode.DO_NOTHING);
        indexingContext.getErrors().addAll(markDownDocument.getErrors());

        // Also index non-documents if referenced and stored locally
        for (DocumentNode node : markDownDocument.getDocumentStructure().flatten(true)) {
          String path = node.getPath();
          if (ignore(path) && !indexingContext.getReferencedLocalResources().contains(path)) {
            indexingContext.getReferencedLocalResources().add(path);
            String body = node.getDescription().trim();
            String tokenized = body.replaceAll("\\W", " ").replaceAll("  ", "");
            if (!body.equals(tokenized))
              body = body + " " + tokenized;
            addToIndex(writer, path, TYPE_OTHER, node.getFileName(), body, new HashMap<>());
          }
        }

        updateReverseIndex(indexingContext.getIndirectReverseIndex(), true, markDownDocument);
        updateReverseIndex(indexingContext.getDirectReverseIndex(), false, markDownDocument);

        addToIndex(writer,  resourcePath, TYPE_DOCUMENT, markDownDocument.getTitle(), markDownDocument.getMarkdown(), markDownDocument.getMetatags());
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  protected void addToIndex(IndexWriter writer, String resourcePath, String resourceType, String title, String contents, Map<String, String> metaTags)
      throws IOException {
    String extension = ThothUtil.getExtension(resourcePath);
    if (extension == null)
      extension = "";
    extension = extension.toLowerCase();

    Document document = new Document();
    document.add(new StringField(INDEX_PATH, resourcePath, Field.Store.YES));
    document.add(new TextField(INDEX_TYPE, resourceType, Store.YES));
    document.add(new TextField(INDEX_TITLE, title, Store.YES));
    document.add(new TextField(INDEX_CONTENTS, contents, Store.NO));
    document.add(new TextField(INDEX_USED, "true", Store.NO));
    document.add(new TextField(INDEX_EXTENSION, extension.toLowerCase(), Store.NO));
    for (Entry<String, String> entry : metaTags.entrySet()) {
      document.add(new TextField(entry.getKey().toLowerCase(), String.valueOf(entry.getValue()), Store.NO));
    }

    if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
      // New index, so we just add the document (no old document can be there):
      LOG.debug("Indexer for context " + contentManager.getContext() + " added " + resourcePath);
      writer.addDocument(document);
    } else {
      // Existing index (an old copy of this document may have been indexed) so
      // we use updateDocument instead to replace the old one matching the exact
      // path, if present:
      LOG.debug("Indexer for context " + contentManager.getContext() + " updated " + resourcePath);
      writer.updateDocument(new Term(INDEX_PATH, resourcePath), document);
    }
  }

  protected void updateReverseIndex(Map<String, List<String>> index, boolean indirect, MarkDownDocument markDownDocument) {
    DocumentNode root = markDownDocument.getDocumentStructure();
    visit(root, index, indirect, new Stack<DocumentNode>());
  }

  protected List<String> getUsageList(DocumentNode node, Map<String, List<String>> reverseIndex) {
    List<String> list = reverseIndex.get(node.getPath());
    if (list == null) {
      list = new ArrayList<>();
      reverseIndex.put(node.getPath(), list);
    }
    return list;
  }

  protected void visit(DocumentNode root, Map<String, List<String>> reverseIndex, boolean indirect, Stack<DocumentNode> stack) {

    if (indirect) {
      for (DocumentNode node : stack) {
        String path = node.getPath();
        addPath(getUsageList(root, reverseIndex), path);
      }
    } else if (!stack.isEmpty()) {
      String path = stack.peek().getPath();
      addPath(getUsageList(root, reverseIndex), path);
    }

    stack.push(root);
    for (DocumentNode child : root.getChildren())
      visit(child, reverseIndex, indirect, stack);
    stack.pop();
  }

  protected void addPath(List<String> usageList, String path) {
    if (!usageList.contains(path))
      usageList.add(path);
  }

  public void setIndexExtensions(String extentions) {
    String[] exts = extentions.split("\\,");
    for (String ext : exts)
      extensions.add(ext.trim().toLowerCase());
  }

  protected boolean ignore(String pathName) {
    if (pathName.startsWith("."))
      return true;
    int idx = pathName.lastIndexOf('.');
    if (idx == -1)
      return false;

    return !extensions.contains(pathName.substring(idx + 1).toLowerCase());
  }

  public Map<String, List<String>> getReverseIndex(String context, boolean indirect) throws ContextNotFoundException, ContentManagerException {
    Map<String, List<String>> reverseIndex = CacheManager.getInstance(context).getReverseIndex(indirect);
    if (reverseIndex == null)
      reverseIndex = new HashMap<>();
    return reverseIndex;
  }

  public List<ProcessorError> getValidationErrors() throws IndexerException {
    List<ProcessorError> validationErrors = CacheManager.getInstance(contentManager.getContext()).getValidationErrors();
    if (validationErrors == null)
      validationErrors = new ArrayList<>();
    return validationErrors;
  }

  class IndexingContext {
    private Map<String, List<String>> indirectReverseIndex = new HashMap<>();
    private Map<String, List<String>> directReverseIndex = new HashMap<>();
    private List<ProcessorError> errors = new ArrayList<>();
    private Set<String> referencedLocalResources = new HashSet<>();

    public Map<String, List<String>> getIndirectReverseIndex() {
      return indirectReverseIndex;
    }

    public Map<String, List<String>> getDirectReverseIndex() {
      return directReverseIndex;
    }

    public List<ProcessorError> getErrors() {
      return errors;
    }

    public Set<String> getReferencedLocalResources() {
      return referencedLocalResources;
    }

  }
}
