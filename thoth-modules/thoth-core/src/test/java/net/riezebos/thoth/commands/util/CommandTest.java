package net.riezebos.thoth.commands.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import net.riezebos.thoth.commands.Command;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.testutil.ThothTestBase;

public class CommandTest extends ThothTestBase {

  protected void testCommand(Command command, String path, String code, String[] htmlExists, String[] jsonExists)
      throws ContextNotFoundException, ContentManagerException, IOException, SkinManagerException, RenderException, UnsupportedEncodingException {
    testCommand(command, path, code, htmlExists, jsonExists, null);
  }

  protected void testCommand(Command command, String path, String code, String[] htmlExists, String[] jsonExists, Map<String, String> args)
      throws ContextNotFoundException, ContentManagerException, IOException, SkinManagerException, RenderException, UnsupportedEncodingException {
    String contextName = "TestContext";
    ContentManager contentManager = registerTestContentManager(contextName);
    Configuration configuration = contentManager.getConfiguration();
    Skin skin = getSkin(contentManager);

    assertEquals(code, command.getTypeCode());
    command.setConfiguration(configuration);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Map<String, Object> arguments = getParameters(contentManager, path);
    if (args != null)
      arguments.putAll(args);
    command.execute(contextName, path, arguments, skin, outputStream);
    String result = outputStream.toString("UTF-8").trim();
    for (String check : htmlExists)
      assertTrue(check, result.indexOf(check) != -1);

    if (jsonExists != null) {
      arguments.put("mode", "json");
      outputStream = new ByteArrayOutputStream();
      command.execute(contextName, path, arguments, skin, outputStream);
      result = outputStream.toString("UTF-8").trim();
      assertTrue(result, result.startsWith("{\"") && result.endsWith("}"));

      for (String check : jsonExists)
        assertTrue(check, result.indexOf(check) != -1);
    }
  }

}
