/* Copyright (c) 2020 W.T.J. Riezebos
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
package net.riezebos.thoth.content.search.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LiveIndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.powermock.api.mockito.PowerMockito;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.search.DocumentContainer;
import net.riezebos.thoth.content.search.Indexer;
import net.riezebos.thoth.content.search.IndexingContext;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.util.ProcessorError;

public class TestIndexer extends Indexer {

  private DocumentContainer documentContainer;
  private List<Document> addDocumentCalls = new ArrayList<>();
  private List<WriteResult> updateDocumentCalls = new ArrayList<>();
  private List<ProcessorError> errors = new ArrayList<>();
  private Map<String, List<String>> indirectReverseIndex = new HashMap<>();
  private Map<String, List<String>> directReverseIndex = new HashMap<>();

  public TestIndexer(ContentManager contentManager) throws ContextNotFoundException, ContentManagerException, IOException {
    super(contentManager);
    documentContainer = new DocumentContainer(contentManager);
  }

  @Override
  protected void persistCaches(IndexingContext indexingContext) {
    directReverseIndex = indexingContext.getDirectReverseIndex();
    indirectReverseIndex = indexingContext.getIndirectReverseIndex();
    errors = new ArrayList<>(indexingContext.getErrors());
    Collections.sort(errors);
  }

  @Override
  protected IndexSearcher getIndexSearcher(IndexReader reader) {

    try {
      IndexSearcher indexSearcher = mock(IndexSearcher.class);
      when(indexSearcher.search(any(Query.class), anyInt(), any(Sort.class))).then(documentContainer.getTopDocs());
      when(indexSearcher.doc(anyInt())).thenAnswer(documentContainer.getDoc());

      return indexSearcher;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  protected IndexReader getIndexReader(String indexFolder) throws IOException {

    IndexReader directoryReader = PowerMockito.mock(IndexReader.class);

    // Mock the final method close() (WTF final guys)
    PowerMockito.doAnswer(invocation -> null).when(directoryReader).close();

    return directoryReader;
  }

  @Override
  protected IndexWriter getWriter(boolean wipeIndex) throws IOException {
    LiveIndexWriterConfig config = mock(LiveIndexWriterConfig.class);
    when(config.getOpenMode()).thenReturn(wipeIndex ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND);

    IndexWriter indexWriter = mock(IndexWriter.class);
    when(indexWriter.getConfig()).thenReturn(config);
    recordAddDocument(indexWriter);
    recordUpdateDocument(indexWriter);

    return indexWriter;
  }

  @SuppressWarnings("unchecked")
  private void recordUpdateDocument(IndexWriter indexWriter) throws IOException {
    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      Term term = (Term) args[0];
      Document document = (Document) args[1];
      updateDocumentCalls.add(new WriteResult(term, document));
      return null;
    }).when(indexWriter).updateDocument(any(Term.class), any(Iterable.class));
  }

  private void recordAddDocument(IndexWriter indexWriter) throws IOException {
    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      Document document = (Document) args[0];
      addDocumentCalls.add(document);
      return null;
    }).when(indexWriter).addDocument(any(Document.class));
  }

  public List<Document> getAddDocumentCalls() {
    return addDocumentCalls;
  }

  public List<WriteResult> getUpdateDocumentCalls() {
    return updateDocumentCalls;
  }

  public List<ProcessorError> getErrors() {
    return errors;
  }

  public Map<String, List<String>> getIndirectReverseIndex() {
    return indirectReverseIndex;
  }

  public Map<String, List<String>> getDirectReverseIndex() {
    return directReverseIndex;
  }
}
