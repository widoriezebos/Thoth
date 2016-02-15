package net.riezebos.thoth.renderers;

import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.configuration.CacheManager;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.testutil.ThothTestBase;

public class HtmlRendererTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    String contextName = "MockedContext";

    CacheManager mockedCacheManager = mockCacheManager();
    Configuration mockedConfiguration = mockConfiguration(mockedCacheManager);
    ContextDefinition mockedContext = mockContextDefinition(contextName);
    ClasspathFileSystem fileSystem = getClasspathFileSystem();
    ContentManager contentManager = getContentManager(mockedConfiguration, mockedContext, fileSystem);
    ContentManagerFactory.registerContentManager(contentManager);
    SkinManager skinManager = new SkinManager(contentManager, null);
    Skin testSkin = skinManager.getSkinByName("TestReposSkin1");
    HtmlRenderer renderer = new HtmlRenderer();
    renderer.setConfiguration(mockedConfiguration);

    String path = "/main/Fourth.md";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Map<String, Object> arguments = getParameters(mockedConfiguration, contextName, testSkin, path);
    renderer.execute(contextName, path, arguments, testSkin, outputStream);
    String result = outputStream.toString("UTF-8").trim();
    System.out.println(result);
    String expected = getExpected("Fourth.expected.html");
    assertEquals(expected, result);
  }

}
