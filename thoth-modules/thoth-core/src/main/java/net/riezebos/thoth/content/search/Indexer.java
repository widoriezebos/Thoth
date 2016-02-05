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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.IndexerException;
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

  private String indexPath;
  private String branch;
  private Path docDir;
  private boolean recreate = true;
  private ContentManager contentManager;
  private Set<String> extensions = new HashSet<>();
  private static Set<String> activeIndexers = new HashSet<>();

  public static void main(String[] args) throws ContentManagerException {
    Indexer indexer = SearchFactory.getInstance().getIndexer("Erasmus");
    indexer.index();
    System.out.println("Done");
  }

  protected Indexer(ContentManager contentManager, String branch) throws BranchNotFoundException, ContentManagerException {
    this.contentManager = contentManager;
    this.branch = branch;
    this.indexPath = contentManager.getIndexFolder(branch);
    this.docDir = Paths.get(contentManager.getBranchFolder(branch));
    this.setIndexExtensions(Configuration.getInstance().getIndexExtensions());
  }

  public void index() throws ContentManagerException {

    synchronized (activeIndexers) {
      if (activeIndexers.contains(this.branch)) {
        LOG.warn("Indexer for branch " + this.branch + " is already (still?) active. Not starting a new index operation");
        return;
      }
      activeIndexers.add(this.branch);
    }

    try {
      Date start = new Date();
      LOG.info("Indexing " + this.branch + " to directory '" + indexPath + "'...");

      IndexWriter writer = getWriter(recreate);
      IndexingContext context = new IndexingContext();
      indexDocs(writer, docDir, context);

      sortIndexLists(context.getIndirectReverseIndex());
      sortIndexLists(context.getDirectReverseIndex());
      Collections.sort(context.getErrors());

      cacheResults(context);

      // NOTE: if you want to maximize search performance,
      // you can optionally call forceMerge here. This can be
      // a terribly costly operation, so generally it's only
      // worth it when your index is relatively static (ie
      // you're done adding documents to it):
      //
      writer.forceMerge(1);

      writer.close();

      markUnusedDocuments(context.getDirectReverseIndex());

      Date end = new Date();
      LOG.info("Indexing branch " + this.branch + " took " + (end.getTime() - start.getTime()) + " milliseconds");
    } catch (IOException e) {
      throw new ContentManagerException(e);
    } finally {
      synchronized (activeIndexers) {
        activeIndexers.remove(this.branch);
      }
    }
  }

  protected IndexWriter getWriter(boolean wipeIndex) throws IOException {
    Directory dir = FSDirectory.open(Paths.get(indexPath));
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

    String indexFolder = contentManager.getIndexFolder(branch);

    try (IndexWriter writer = getWriter(false); IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexFolder)))) {
      IndexSearcher searcher = new IndexSearcher(reader);
      for (ContentNode node : ContentManagerFactory.getContentManager().getUnusedFragments(branch)) {
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

  protected void cacheResults(IndexingContext context) throws BranchNotFoundException, IOException, FileNotFoundException {
    String reverseIndexFile = contentManager.getReverseIndexFileName(branch);
    String indirectReverseIndexFile = contentManager.getReverseIndexIndirectFileName(branch);
    String errorFile = contentManager.getErrorFileName(branch);

    synchronized (CacheManager.getFileLock()) {
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(reverseIndexFile)))) {
        oos.writeObject(context.getDirectReverseIndex());
      }
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(indirectReverseIndexFile)))) {
        oos.writeObject(context.getIndirectReverseIndex());
      }
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(errorFile)))) {
        oos.writeObject(context.getErrors());
      }
    }

    CacheManager cacheManager = CacheManager.getInstance(branch);
    cacheManager.cacheReverseIndex(true, context.getIndirectReverseIndex());
    cacheManager.cacheReverseIndex(false, context.getDirectReverseIndex());
    cacheManager.cacheErrors(context.getErrors());
  }

  protected void sortIndexLists(Map<String, List<String>> map) {
    for (Entry<String, List<String>> entry : map.entrySet())
      Collections.sort(entry.getValue());
  }

  void indexDocs(final IndexWriter writer, Path path, final IndexingContext context) throws IOException, BranchNotFoundException {

    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
            indexDoc(writer, file, attrs.lastModifiedTime().toMillis(), context);
          } catch (Exception ignore) {
            // don't index files that can't be read.
            LOG.warn("Not indexing " + file.toString() + " because of " + ignore.getMessage());
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis(), context);
    }
  }

  /**
   * Indexes a single document
   * 
   * @param indirectReverseIndex
   * @param directReverseIndex
   * @param referencedLocalResources
   * @param errors
   * @throws BranchNotFoundException
   */
  void indexDoc(IndexWriter writer, Path file, long lastModified, IndexingContext context) throws IOException, BranchNotFoundException {

    Path relativePath = docDir.relativize(file);
    if (!ignore(relativePath.toString())) {
      // make a new, empty document

      try {
        String resourcePath = relativePath.toString();
        MarkDownDocument markDownDocument = contentManager.getMarkDownDocument(branch, resourcePath);
        context.getErrors().addAll(markDownDocument.getErrors());

        // Also index non-documents if referenced and stored locally
        for (DocumentNode node : markDownDocument.getDocumentStructure().flatten(true)) {
          String path = node.getPath();
          if (ignore(path) && !context.getReferencedLocalResources().contains(path)) {
            context.getReferencedLocalResources().add(path);
            String body = node.getDescription().trim();
            String tokenized = body.replaceAll("\\W", " ").replaceAll("  ", "");
            if (!body.equals(tokenized))
              body = body + " " + tokenized;
            addToIndex(writer, path, TYPE_OTHER, node.getFileName(), body, new HashMap<>());
          }
        }

        updateReverseIndex(context.getIndirectReverseIndex(), true, markDownDocument);
        updateReverseIndex(context.getDirectReverseIndex(), false, markDownDocument);

        addToIndex(writer, "/" + resourcePath, TYPE_DOCUMENT, markDownDocument.getTitle(), markDownDocument.getMarkdown(), markDownDocument.getMetatags());
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
      LOG.debug("Indexer for branch " + this.branch + " added " + resourcePath);
      writer.addDocument(document);
    } else {
      // Existing index (an old copy of this document may have been indexed) so
      // we use updateDocument instead to replace the old one matching the exact
      // path, if present:
      LOG.debug("Indexer for branch " + this.branch + " updated " + resourcePath);
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

  public Map<String, List<String>> getReverseIndex(String branch, boolean indirect) throws BranchNotFoundException, ContentManagerException {
    return CacheManager.getInstance(branch).getReverseIndex(indirect);
  }

  public List<ProcessorError> getValidationErrors() throws IndexerException {
    return CacheManager.getInstance(branch).getValidationErrors();
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
