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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.search.util.TestSearcher;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.util.DocumentNode;
import net.riezebos.thoth.testutil.ThothTestBase;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;
import net.riezebos.thoth.util.PagedList;

/*
 * We need to use PowerMock here because for some very intelligent reason (no doubt) the guys from Lucene decided to make the IndexReader.close() method final.
 * Which makes it impossible to mock because it will throw a NPE for any mock. See TestIndexer.getIndexReader() for more.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(IndexReader.class)
public class SearcherTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    String contextName = "searchtest";
    ThothEnvironment thothEnvironment = createThothContext(contextName);
    UserManager userManager = thothEnvironment.getUserManager();
    User user = userManager.getUser("administrator");

    ContentManager contentManager = createTestContentManager(thothEnvironment, contextName);
    Searcher searcher = new TestSearcher(contentManager);

    PagedList<SearchResult> search = searcher.search(user, "Main.md", 0, 25);
    List<SearchResult> list = search.getList();
    assertEquals(1, list.size());
    SearchResult searchResult = list.get(0);
    assertEquals("/main/Main.md", searchResult.getDocument());
    assertEquals(1, searchResult.getFragments().size());
    assertEquals(1, searchResult.getBookReferences().size());
    assertFalse(searchResult.isResource());
    DocumentNode documentNode = searchResult.getBookReferences().get(0);
    assertEquals("/books/Main.book", documentNode.getPath());
    Fragment fragment = searchResult.getFragments().get(0);
    assertTrue(fragment.getText().indexOf("Main.md") != -1);

    search = searcher.search(user, "tip.png", 0, 25);
    list = search.getList();
    assertEquals(1, list.size());
    searchResult = list.get(0);
    assertTrue(searchResult.isResource());
  }

}
