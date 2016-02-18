package net.riezebos.thoth.content.search.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.search.DocumentContainer;
import net.riezebos.thoth.content.search.Searcher;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class TestSearcher extends Searcher {

  private DocumentContainer documentContainer;

  public TestSearcher(ContentManager contentManager) {
    super(contentManager);
    try {
      documentContainer = new DocumentContainer(contentManager);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  protected IndexReader getIndexReader(ContentManager contentManager) throws ContextNotFoundException, IOException {

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

}
