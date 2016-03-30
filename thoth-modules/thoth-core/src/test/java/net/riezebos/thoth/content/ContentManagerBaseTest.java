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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.markdown.critics.CriticProcessingMode;
import net.riezebos.thoth.testutil.ThothTestBase;

public class ContentManagerBaseTest extends ThothTestBase {

  @Test
  public void testContentManagerBase() throws ContentManagerException, IOException {
    String BUILT_IN_CSS_MARKER = "Avenir Next";

    String contextName = "MockedContext";
    ThothEnvironment thothEnvironment = createThothContext(contextName);
    ContentManager contentManager = createTestContentManager(thothEnvironment, contextName);

    Skin testReposSkin2 = contentManager.getSkinManager().getSkinByName("TestReposSkin2");

    assertEquals("/library/TestReposSkin2/", testReposSkin2.getSkinBaseFolder());
    assertEquals("MockedContext/library/TestReposSkin2", testReposSkin2.getBaseUrl());
    assertEquals("MockedContext: classpath based. Will do nothing", contentManager.refresh());

    assertEquals(2, contentManager.getBooks().size());

    assertTrue(getAsString(contentManager, "skins.properties").indexOf("TestReposSkin") != -1);
    assertTrue(getAsString(contentManager, "library/TestReposSkin/Webresources/a.txt").indexOf("a1") != -1);
    // Check inheritance of (skin related) files
    assertTrue(getAsString(contentManager, "library/TestReposSkin/Webresources/style.css").indexOf(BUILT_IN_CSS_MARKER) != -1);
    assertTrue(getAsString(contentManager, "library/TestReposSkin/Webresources/markdown.css").indexOf("TestReposSkin CSS") != -1);
    assertNull(getAsString(contentManager, "library/TestReposSkin/Webresources/b.txt"));
    // Check inheritance of (skin related) files
    assertTrue(getAsString(contentManager, "library/TestReposSkin2/Webresources/style.css").indexOf(BUILT_IN_CSS_MARKER) != -1);
    assertTrue(getAsString(contentManager, "library/TestReposSkin2/Webresources/markdown.css").indexOf("TestReposSkin2 CSS") != -1);
    assertTrue(getAsString(contentManager, "library/TestReposSkin2/Webresources/b.txt").indexOf("b1") != -1);

    assertEquals("/some/workspace/MockedContext-index/lucene/", contentManager.getIndexFolder());
    assertEquals("/some/workspace/MockedContext-index/reverseindex.bin", contentManager.getReverseIndexFileName());
    assertEquals("/some/workspace/MockedContext-index/errors.bin", contentManager.getErrorFileName());
    assertEquals("/some/workspace/MockedContext-index/indirectreverseindex.bin", contentManager.getReverseIndexIndirectFileName());

    MarkDownDocument markDownDocument = contentManager.getMarkDownDocument("/books/Main.book", false, CriticProcessingMode.PROCESS);
    String markdown = markDownDocument.getMarkdown().replaceAll("\t", "    ");
    String expected = getExpected("Main.book.expected.md");
    assertTrue(stringsEqual(expected.trim(), markdown.trim()));

    markDownDocument = contentManager.getMarkDownDocument("/books/Second.book", false, CriticProcessingMode.PROCESS);
    markdown = markDownDocument.getMarkdown().replaceAll("\t", "    ");
    expected = getExpected("Second.book.expected.md");
    assertTrue(stringsEqual(expected.trim(), markdown.trim()));
    assertEquals(39022364121L, contentManager.getContextChecksum());
    assertEquals(2, contentManager.list("/books").size());
    assertEquals(1, contentManager.list("/images").size());
    assertEquals(1, contentManager.list("/images/tip.png").size());
    assertTrue(contentManager.isFragment("/main/Main.md"));

    List<ContentNode> unusedFragments = contentManager.getUnusedFragments();
    assertTrue(containsNode(unusedFragments, "/main/NotReferenced.md"));
  }

  protected boolean containsNode(List<ContentNode> nodes, String path) {
    for (ContentNode node : nodes)
      if (node.getPath().equals(path))
        return true;
    return false;
  }
}
