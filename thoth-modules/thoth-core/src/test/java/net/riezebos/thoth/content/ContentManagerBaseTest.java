package net.riezebos.thoth.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.CacheManager;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.markdown.critics.CriticProcessingMode;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.testutil.ThothTestBase;

public class ContentManagerBaseTest extends ThothTestBase {

  @Test
  public void testContentManagerBase() throws ContentManagerException, IOException {

    CacheManager mockedCacheManager = mockCacheManager();
    Configuration mockedConfiguration = mockConfiguration(mockedCacheManager);
    ContextDefinition mockedContext = mockContextDefinition("MockedContext");
    ClasspathFileSystem fileSystem = getClasspathFileSystem();
    ContentManager contentManager = getContentManager(mockedConfiguration, mockedContext, fileSystem);

    assertEquals("MockedContext: classpath based. Will do nothing", contentManager.refresh());

    assertEquals(2, contentManager.getBooks().size());

    assertTrue(getAsString(contentManager, "skins.properties").indexOf("TestReposSkin") != -1);
    assertTrue(getAsString(contentManager, "library/TestReposSkin/Webresources/a.txt").indexOf("a1") != -1);
    // Check inheritance of (skin related) files
    assertTrue(getAsString(contentManager, "library/TestReposSkin/Webresources/style.css").indexOf("Thoth Documentation System") != -1);
    assertTrue(getAsString(contentManager, "library/TestReposSkin/Webresources/markdown.css").indexOf("TestReposSkin CSS") != -1);
    assertNull(getAsString(contentManager, "library/TestReposSkin/Webresources/b.txt"));
    // Check inheritance of (skin related) files
    assertTrue(getAsString(contentManager, "library/TestReposSkin2/Webresources/style.css").indexOf("Thoth Documentation System") != -1);
    assertTrue(getAsString(contentManager, "library/TestReposSkin2/Webresources/markdown.css").indexOf("TestReposSkin2 CSS") != -1);
    assertTrue(getAsString(contentManager, "library/TestReposSkin2/Webresources/b.txt").indexOf("b1") != -1);

    assertEquals("/some/workspace/MockedContext-index/lucene/", contentManager.getIndexFolder());
    assertEquals("/some/workspace/MockedContext-index/reverseindex.bin", contentManager.getReverseIndexFileName());
    assertEquals("/some/workspace/MockedContext-index/errors.bin", contentManager.getErrorFileName());
    assertEquals("/some/workspace/MockedContext-index/indirectreverseindex.bin", contentManager.getReverseIndexIndirectFileName());

    MarkDownDocument markDownDocument = contentManager.getMarkDownDocument("/books/Main.book", false, CriticProcessingMode.PROCESS);
    String markdown = markDownDocument.getMarkdown().replaceAll("\t", "    ");
    String expected = getExpected("Main.book.expected.md");
    assertEquals(expected.trim(), markdown.trim());

    markDownDocument = contentManager.getMarkDownDocument("/books/Second.book", false, CriticProcessingMode.PROCESS);
    markdown = markDownDocument.getMarkdown().replaceAll("\t", "    ");
    expected = getExpected("Second.book.expected.md");
    assertEquals(expected.trim(), markdown.trim());
    assertEquals(37095086148L, contentManager.getContextChecksum());
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
