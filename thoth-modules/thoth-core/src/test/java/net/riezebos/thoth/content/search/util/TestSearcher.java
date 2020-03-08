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
    PowerMockito.doAnswer(invocation -> null).when(directoryReader).close();

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
