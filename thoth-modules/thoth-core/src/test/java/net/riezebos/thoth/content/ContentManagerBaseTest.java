package net.riezebos.thoth.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.CacheManager;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.content.impl.ClasspathContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.markdown.critics.CriticProcessingMode;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.util.ThothUtil;

public class ContentManagerBaseTest {

  @Test
  public void test() throws ContentManagerException, IOException {

    String contextName = "MockedContext";
    CacheManager mockedCacheManager = mock(CacheManager.class);
    when(mockedCacheManager.getReverseIndex(true)).thenReturn(getReverseIndexIndirect());
    when(mockedCacheManager.getReverseIndex(false)).thenReturn(getReverseIndex());

    Configuration mockedConfiguration = mock(Configuration.class);
    when(mockedConfiguration.getWorkspaceLocation()).thenReturn("/some/workspace/");
    when(mockedConfiguration.getDefaultSkin()).thenReturn("SimpleSkin");
    when(mockedConfiguration.appendErrors()).thenReturn(true);
    when(mockedConfiguration.getBookExtensions()).thenReturn(Arrays.asList(new String[] {"book"}));
    when(mockedConfiguration.getDocumentExtensions()).thenReturn(Arrays.asList(new String[] {"md"}));
    when(mockedConfiguration.getImageExtensions()).thenReturn("png");
    when(mockedConfiguration.isFragment(anyString())).thenReturn(true);
    when(mockedConfiguration.getCacheManager(anyString())).thenReturn(mockedCacheManager);

    ContextDefinition mockedContext = mock(ContextDefinition.class);
    when(mockedContext.getName()).thenReturn(contextName);
    when(mockedContext.getRefreshIntervalMS()).thenReturn(0L);

    String location = "net/riezebos/thoth/content/testrepos";
    ClasspathFileSystem fileSystem = new ClasspathFileSystem(location);
    fileSystem.registerFiles("net/riezebos/thoth/content/testrepos.lst");

    ContentManagerBase contentManager = new ClasspathContentManager(mockedContext, mockedConfiguration, fileSystem);
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
    assertEquals(4078283432L, contentManager.getContextChecksum());
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

  protected Map<String, List<String>> getReverseIndex() {
    Map<String, List<String>> result = new HashMap<>();
    result.put("/main/subs/SubOne.md", Arrays.asList(new String[] {"/main/Main.md"}));
    result.put("/main/Main.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Second.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Second.book"}));
    result.put("/main/Third.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Fourth.md", Arrays.asList(new String[] {"/books/Second.book"}));
    return result;
  }

  protected Map<String, List<String>> getReverseIndexIndirect() {
    Map<String, List<String>> result = new HashMap<>();
    result.put("/main/subs/SubOne.md", Arrays.asList(new String[] {"/main/Main.md", "/books/Main.book"}));
    result.put("/main/Main.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Second.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Second.book"}));
    result.put("/main/Third.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Main.book"}));
    result.put("/main/Fourth.md", Arrays.asList(new String[] {"/books/Second.book"}));
    return result;
  }

  protected String getExpected(String path) throws IOException {
    path = "net/riezebos/thoth/content/expected/" + path;
    InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    if (resourceAsStream == null)
      throw new FileNotFoundException(path + " not found");
    return ThothUtil.readInputStream(resourceAsStream);
  }

  protected String getAsString(ContentManagerBase contentManager, String path) throws IOException {
    InputStream inputStream = contentManager.getInputStream(path);
    if (inputStream == null)
      return null;
    return ThothUtil.readInputStream(inputStream);
  }

}
