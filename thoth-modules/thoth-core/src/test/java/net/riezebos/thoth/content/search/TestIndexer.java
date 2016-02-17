package net.riezebos.thoth.content.search;

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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LiveIndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.configuration.IndexingContext;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.util.ProcessorError;

public class TestIndexer extends Indexer {

  private List<Document> docs = new ArrayList<>();
  private List<Document> addDocumentCalls = new ArrayList<>();
  private List<WriteResult> updateDocumentCalls = new ArrayList<>();
  private List<String> knownDocuments;
  private List<ProcessorError> errors = new ArrayList<>();
  private Map<String, List<String>> indirectReverseIndex = new HashMap<>();
  private Map<String, List<String>> directReverseIndex = new HashMap<>();

  public TestIndexer(ContentManager contentManager) throws ContextNotFoundException, ContentManagerException, IOException {
    super(contentManager);
    knownDocuments = new ArrayList<>();
    docs = new ArrayList<>();
    for (ContentNode node : contentManager.find("*.*", true)) {
      knownDocuments.add(node.getPath());
    }
    Collections.sort(knownDocuments);
    for (String resourcePath : knownDocuments) {
      Document document = new Document();
      document.add(new StringField(INDEX_PATH, resourcePath, Field.Store.YES));
      docs.add(document);
    }
  }

  public void resetTest() {
    docs = new ArrayList<>();
    addDocumentCalls = new ArrayList<>();
    updateDocumentCalls = new ArrayList<>();
    errors = new ArrayList<>();
    indirectReverseIndex = new HashMap<>();
    directReverseIndex = new HashMap<>();
  }

  @Override
  protected void persistCaches(IndexingContext indexingContext) {
    directReverseIndex = indexingContext.getDirectReverseIndex();
    indirectReverseIndex = indexingContext.getIndirectReverseIndex();
    errors = indexingContext.getErrors();
  }

  protected IndexSearcher getIndexSearcher(IndexReader reader) {

    try {
      IndexSearcher indexSearcher = mock(IndexSearcher.class);
      when(indexSearcher.search(any(Query.class), anyInt(), any(Sort.class))).then(getTopDocs());
      when(indexSearcher.doc(anyInt())).thenAnswer(getDoc());

      return indexSearcher;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected Answer<TopFieldDocs> getTopDocs() {

    return new Answer<TopFieldDocs>() {

      @Override
      public TopFieldDocs answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        TermQuery query = (TermQuery) args[0];
        Term term = query.getTerm();
        String path = term.bytes().utf8ToString();
        int size = 1;
        ScoreDoc[] scoreDocs = new ScoreDoc[0];
        int idx = knownDocuments.indexOf(path);
        if (idx == -1)
          size = 0;
        else {
          scoreDocs = new ScoreDoc[1];
          scoreDocs[0] = new ScoreDoc(idx, 1);
        }

        TopFieldDocs topDocs = new TopFieldDocs(size, scoreDocs, null, 0);
        return topDocs;
      }
    };
  }

  protected IndexReader getIndexReader(String indexFolder) throws IOException {

    IndexReader directoryReader = PowerMockito.mock(IndexReader.class);

    // Mock the final method close() (WTF final guys)
    PowerMockito.doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(directoryReader).close();

    return directoryReader;
  }

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
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        Term term = (Term) args[0];
        Document document = (Document) args[1];
        updateDocumentCalls.add(new WriteResult(term, document));
        return null;
      }
    }).when(indexWriter).updateDocument(any(Term.class), any(Iterable.class));
  }

  private void recordAddDocument(IndexWriter indexWriter) throws IOException {
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        Document document = (Document) args[0];
        addDocumentCalls.add(document);
        return null;
      }
    }).when(indexWriter).addDocument(any(Document.class));
  }

  protected Answer<Document> getDoc() {
    return new Answer<Document>() {

      @Override
      public Document answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        Integer index = (Integer) args[0];
        return docs.get(index);
      }
    };
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

  class WriteResult {
    private Term term;
    private Document document;

    public WriteResult(Term term, Document document) {
      this.term = term;
      this.document = document;
    }

    public Term getTerm() {
      return term;
    }

    public Document getDocument() {
      return document;
    }
  }
}
