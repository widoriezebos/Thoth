package net.riezebos.thoth.commands.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import net.riezebos.thoth.commands.Command;
import net.riezebos.thoth.commands.CommandOperation;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.renderers.HtmlRenderer;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.testutil.ThothTestBase;

public class CommandTest extends ThothTestBase implements RendererProvider {

  private ContentManager contentManager;
  private ThothEnvironment thothEnvironment;
  private String contextName = "TestContext";

  protected void testCommand(Command command, String path, String code, String[] htmlExists, String[] jsonExists)
      throws ContextNotFoundException, ContentManagerException, IOException, SkinManagerException, RenderException, UnsupportedEncodingException {
    testCommand(command, path, CommandOperation.GET, code, htmlExists, jsonExists, null);
  }

  protected RenderResult testCommand(Command command, String path, CommandOperation commandOperation, String code, String[] htmlExists, String[] jsonExists,
      Map<String, String> args)
          throws ContextNotFoundException, ContentManagerException, IOException, SkinManagerException, RenderException, UnsupportedEncodingException {

    Skin skin = getSkin(contentManager);

    assertEquals(code, command.getTypeCode());

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Map<String, Object> arguments = getParameters(contentManager, path);
    if (args != null)
      arguments.putAll(args);

    RenderResult renderResult = RenderResult.OK;
    if (htmlExists != null) {
      renderResult = command.execute(getCurrentUser(thothEnvironment), contextName, path, commandOperation, arguments, skin, outputStream);
      String result = outputStream.toString("UTF-8").trim();
      for (String check : htmlExists) {
        boolean condition = result.indexOf(check) != -1;
        if (!condition)
          System.out.println(result + "\ndoes not contain\n" + check);
        assertTrue(check, condition);
      }
    }
    if (jsonExists != null) {
      arguments.put("mode", "json");
      outputStream = new ByteArrayOutputStream();
      renderResult = command.execute(getCurrentUser(thothEnvironment), contextName, path, commandOperation, arguments, skin, outputStream);
      String result = outputStream.toString("UTF-8").trim();
      assertTrue(result, result.startsWith("{\"") && result.endsWith("}"));

      for (String check : jsonExists)
        assertTrue(check, result.indexOf(check) != -1);
    }
    return renderResult;
  }

  protected ThothEnvironment setupContentManager() throws ContextNotFoundException, ContentManagerException, IOException {
    thothEnvironment = createThothContext(contextName);
    contentManager = createTestContentManager(thothEnvironment, contextName);
    thothEnvironment.registerContentManager(contentManager);
    return thothEnvironment;
  }

  @Override
  public Renderer getRenderer(String typeCode) {
    try {
      return new HtmlRenderer(createThothContext("dummy"), this);
    } catch (ContentManagerException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected ThothEnvironment getThothEnvironment() {
    return thothEnvironment;
  }
}
