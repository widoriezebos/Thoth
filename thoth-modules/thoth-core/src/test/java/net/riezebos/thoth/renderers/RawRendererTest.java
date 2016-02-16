package net.riezebos.thoth.renderers;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.renderers.Renderer.RenderResult;
import net.riezebos.thoth.testutil.ThothTestBase;

public class RawRendererTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    String contextName = "TestContext";

    ContentManager contentManager = registerTestContentManager(contextName);
    Configuration configuration = contentManager.getConfiguration();
    SkinManager skinManager = contentManager.getSkinManager();
    Skin testSkin = skinManager.getSkinByName("TestReposSkin1");

    RawRenderer renderer = new RawRenderer();
    renderer.setConfiguration(configuration);

    String path = "/main/Fourth.md";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Map<String, Object> arguments = getParameters(configuration, contextName, testSkin, path);
    renderer.execute(contextName, path, arguments, testSkin, outputStream);
    String result = outputStream.toString("UTF-8").trim();
    String expected = getExpected("Fourth.expected.md");
    assertEquals(expected, result);
    assertEquals("text/plain;charset=UTF-8", renderer.getContentType(new HashMap<String, Object>()));
    assertEquals(RawRenderer.TYPE, renderer.getTypeCode());

    RenderResult renderResult = renderer.execute(contextName, "/wrong/path.md", arguments, testSkin, outputStream);
    assertEquals(RenderResult.NOT_FOUND, renderResult);
  }

}
