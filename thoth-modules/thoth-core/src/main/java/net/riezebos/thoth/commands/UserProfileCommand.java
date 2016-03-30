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
package net.riezebos.thoth.commands;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;

public class UserProfileCommand extends RendererBase implements Command {

  public static final String ARG_FIRSTNAME = "firstname";
  public static final String ARG_LASTNAME = "lastname";
  public static final String ARG_PASSWORD = "password";
  public static final String ARG_PASSWORD2 = "password2";

  public UserProfileCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "userprofile";
  }

  @Override
  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {
      RenderResult result = RenderResult.OK;
      Boolean loggedIn = (Boolean) arguments.get(Renderer.LOGGED_IN);
      if (loggedIn == null)
        loggedIn = false;

      if (!loggedIn)
        return RenderResult.FORBIDDEN;

      if (CommandOperation.POST.equals(operation))
        result = handleOperation(identity, contextName, path, operation, arguments, skin, outputStream);
      else
        result = handleRender(identity, contextName, path, operation, arguments, skin, outputStream);

      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  protected RenderResult handleOperation(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments,
      Skin skin, OutputStream outputStream) throws Exception {

    String identifier = (String) arguments.get(Renderer.IDENTITY);
    String firstname = (String) arguments.get(ARG_FIRSTNAME);
    String lastname = (String) arguments.get(ARG_LASTNAME);
    String password = (String) arguments.get(ARG_PASSWORD);
    String password2 = (String) arguments.get(ARG_PASSWORD2);

    UserManager userManager = getThothEnvironment().getUserManager();
    User user = userManager.getUser(identifier);
    if (user == null) {
      arguments.put("message", "User with identifier " + identifier + " not found");
    } else {
      boolean valid = true;

      user.setFirstname(firstname);
      user.setLastname(lastname);
      if (StringUtils.isNotBlank(password)) {
        if (!password.equals(password2)) {
          arguments.put("message", "Passwords to not match");
          valid = false;
        } else
          user.setPassword(password);
      }
      if (valid) {
        userManager.updateUser(user);
        arguments.put("message", "Profile updated");
      }
    }
    return handleRender(identity, contextName, path, operation, arguments, skin, outputStream);
  }

  protected RenderResult handleRender(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws Exception {
    Map<String, Object> variables = new HashMap<>(arguments);

    String identifier = (String) arguments.get(Renderer.IDENTITY);
    UserManager userManager = getThothEnvironment().getUserManager();
    User user = userManager.getUser(identifier);

    variables.put("user", user);
    if (asJson(arguments))
      executeJson(variables, outputStream);
    else {
      String userProfileTemplate = skin.getUserProfileTemplate();
      renderTemplate(userProfileTemplate, null, variables, outputStream);
    }
    return RenderResult.OK;
  }
}
