package net.riezebos.thoth.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import net.riezebos.thoth.beans.MarkDownDocument;
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

    Configuration mockedConfiguration = mock(Configuration.class);
    when(mockedConfiguration.getDefaultSkin()).thenReturn("SimpleSkin");
    when(mockedConfiguration.appendErrors()).thenReturn(true);

    ContextDefinition mockedContext = mock(ContextDefinition.class);
    when(mockedContext.getName()).thenReturn("MockedContext");
    when(mockedContext.getRefreshIntervalMS()).thenReturn(0L);

    String location = "net/riezebos/thoth/content/testrepos";
    ClasspathFileSystem fileSystem = new ClasspathFileSystem(location);
    fileSystem.registerFiles("net/riezebos/thoth/content/testrepos.lst");

    ContentManagerBase contentManager = new ClasspathContentManager(mockedContext, mockedConfiguration, fileSystem);
    assertEquals("MockedContext: classpath based. Will do nothing", contentManager.refresh());

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

    MarkDownDocument markDownDocument = contentManager.getMarkDownDocument("/books/Main.book", false, CriticProcessingMode.PROCESS);
    String markdown = markDownDocument.getMarkdown().replaceAll("\t", "    ");
    System.out.println(markdown);
    String expected = getResourceAsString("net/riezebos/thoth/content/expected/Main.book.expected.md");
    assertEquals(expected.trim(), markdown.trim());

  }

  protected String getResourceAsString(String path) throws IOException {
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
