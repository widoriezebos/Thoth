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
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.CacheManager;
import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.markdown.util.DocumentNode;
import net.riezebos.thoth.content.markdown.util.ProcessorError;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.IndexerException;

public class Indexer {
  public static final String INDEX_CONTENTS = "contents";
  public static final String INDEX_TITLE = "title";
  public static final String INDEX_PATH = "path";
  public static final String INDEX_MODIFIED = "modified";

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

      Directory dir = FSDirectory.open(Paths.get(indexPath));
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

      if (recreate) {
        // Create a new index in the directory, removing any
        // previously indexed documents:
        iwc.setOpenMode(OpenMode.CREATE);
      } else {
        // Add new documents to an existing index:
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
      }

      // Optional: for better indexing performance, if you
      // are indexing many documents, increase the RAM
      // buffer. But if you do this, increase the max heap
      // size to the JVM (eg add -Xmx512m or -Xmx1g):
      //
      // iwc.setRAMBufferSizeMB(256.0);

      IndexWriter writer = new IndexWriter(dir, iwc);

      Map<String, List<String>> indirectReverseIndex = new HashMap<>();
      Map<String, List<String>> directReverseIndex = new HashMap<>();
      List<ProcessorError> errors = new ArrayList<>();

      indexDocs(writer, docDir, indirectReverseIndex, directReverseIndex, errors);

      sortIndexLists(indirectReverseIndex);
      sortIndexLists(directReverseIndex);
      Collections.sort(errors);

      String reverseIndexFile = contentManager.getReverseIndexFileName(branch);
      String indirectReverseIndexFile = contentManager.getReverseIndexIndirectFileName(branch);
      String errorFile = contentManager.getErrorFileName(branch);

      synchronized (CacheManager.getFileLock()) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(reverseIndexFile)))) {
          oos.writeObject(directReverseIndex);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(indirectReverseIndexFile)))) {
          oos.writeObject(indirectReverseIndex);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(errorFile)))) {
          oos.writeObject(errors);
        }
      }

      CacheManager cacheManager = CacheManager.getInstance(branch);
      cacheManager.cacheReverseIndex(true, indirectReverseIndex);
      cacheManager.cacheReverseIndex(false, directReverseIndex);
      cacheManager.cacheErrors(errors);

      // NOTE: if you want to maximize search performance,
      // you can optionally call forceMerge here. This can be
      // a terribly costly operation, so generally it's only
      // worth it when your index is relatively static (ie
      // you're done adding documents to it):
      //
      writer.forceMerge(1);

      writer.close();

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

  protected void sortIndexLists(Map<String, List<String>> map) {
    for (Entry<String, List<String>> entry : map.entrySet())
      Collections.sort(entry.getValue());
  }

  void indexDocs(final IndexWriter writer, Path path, final Map<String, List<String>> indirectReverseIndex, final Map<String, List<String>> directReverseIndex,
      final List<ProcessorError> errors) throws IOException, BranchNotFoundException {

    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          try {
            indexDoc(writer, file, attrs.lastModifiedTime().toMillis(), indirectReverseIndex, directReverseIndex, errors);
          } catch (Exception ignore) {
            // don't index files that can't be read.
            LOG.warn("Not indexing " + file.toString() + " because of " + ignore.getMessage());
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis(), indirectReverseIndex, directReverseIndex, errors);
    }
  }

  /**
   * Indexes a single document
   * 
   * @param indirectReverseIndex
   * @param directReverseIndex
   * @param errors
   * @throws BranchNotFoundException
   */
  void indexDoc(IndexWriter writer, Path file, long lastModified, Map<String, List<String>> indirectReverseIndex, Map<String, List<String>> directReverseIndex,
      List<ProcessorError> errors) throws IOException, BranchNotFoundException {

    Path relativePath = docDir.relativize(file);
    if (!ignore(relativePath)) {
      // make a new, empty document
      Document doc = new Document();

      try {
        MarkDownDocument markDownDocument = contentManager.getMarkDownDocument(branch, relativePath.toString());
        errors.addAll(markDownDocument.getErrors());

        doc.add(new StringField(INDEX_PATH, "/" + relativePath.toString(), Field.Store.YES));
        doc.add(new TextField(INDEX_TITLE, markDownDocument.getTitle(), Store.YES));
        doc.add(new LongField(INDEX_MODIFIED, lastModified, Field.Store.NO));
        doc.add(new TextField(INDEX_CONTENTS, markDownDocument.getMarkdown(), Store.NO));

        updateReverseIndex(indirectReverseIndex, true, markDownDocument);
        updateReverseIndex(directReverseIndex, false, markDownDocument);

        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
          // New index, so we just add the document (no old document can be there):
          LOG.debug("Indexer for branch " + this.branch + " added " + relativePath);
          writer.addDocument(doc);
        } else {
          // Existing index (an old copy of this document may have been indexed) so
          // we use updateDocument instead to replace the old one matching the exact
          // path, if present:
          LOG.debug("Indexer for branch " + this.branch + " updated " + relativePath);
          writer.updateDocument(new Term(INDEX_PATH, relativePath.toString()), doc);
        }
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
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

  protected boolean ignore(Path path) {
    String pathName = path.toString();
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
}
