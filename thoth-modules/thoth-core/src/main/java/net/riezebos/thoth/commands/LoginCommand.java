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

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.User;

public class LoginCommand extends RendererBase implements Command {

  public static final String USER_ARGUMENT = "user";

  public LoginCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "login";
  }

  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {

      Map<String, Object> variables = new HashMap<>(arguments);
      User user = null;

      RenderResult result = RenderResult.OK;
      String message = null;
      boolean loggedin = false;
      if (operation.equals(CommandOperation.POST)) {

        String username = (String) arguments.get("username");
        String password = (String) arguments.get("password");

        user = getThothEnvironment().getUserManager().getUser(username);
        if (user != null) {
          loggedin = user.isValidPassword(password);
        }
        if (!loggedin)
          message = "Invalid username and/or password.";
      }
      variables.put("message", message);
      if (!loggedin) {
        if (asJson(arguments))
          executeJson(variables, outputStream);
        else {
          String loginTemplate = skin.getLoginTemplate();
          renderTemplate(loginTemplate, null, variables, outputStream);
        }
      } else {
        Map<String, Object> args = new HashMap<>();
        args.put(USER_ARGUMENT, user);
        result = RenderResult.LOGGED_IN(args);
      }

      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
