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
import net.riezebos.thoth.testutil.ThothTestBase;

public class CustomRendererTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    String contextName = "TestContext";
    String contentType = "application/pdf";
    String pdf = "pdf";

    ContentManager contentManager = registerTestContentManager(contextName);
    Configuration configuration = contentManager.getConfiguration();
    SkinManager skinManager = contentManager.getSkinManager();
    Skin testSkin = skinManager.getSkinByName("TestReposSkin1");

    TestCustomRenderer renderer = new TestCustomRenderer();
    renderer.setContentType(contentType);
    renderer.setTypeCode(pdf);
    String commandLine = "output={${output}} url={${url}}";
    renderer.setCommandLine(commandLine);
    renderer.setConfiguration(configuration);

    String path = "/main/Fourth.md";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Map<String, Object> arguments = getParameters(configuration, contextName, testSkin, path);
    renderer.execute(contextName, path, arguments, testSkin, outputStream);
    String result = outputStream.toString("UTF-8").trim();
    assertEquals("rendered", result);
    assertEquals(contentType, renderer.getContentType(new HashMap<String, Object>()));
    assertEquals(pdf, renderer.getTypeCode());

  }

}
