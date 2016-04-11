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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.util.ExpiringCache;

public class LoginCommand extends RendererBase implements Command {
  private static final Logger LOG = LoggerFactory.getLogger(LoginCommand.class);

  public static final String TYPE_CODE = "login";
  public static final String SORRY_YOUR_ACCOUNT_IS_LOCKED_UNTIL = "Sorry, your account is locked until ";
  public static final String INVALID_USERNAME_AND_OR_PASSWORD = "Invalid username and/or password.";
  public static final String USER_ARGUMENT = "user";

  private static int MAX_LOGIN_ATTEMPTS = 5;
  private static long BLOCK_DURATION = 2 * 60 * 1000; // Two minutes

  public LoginCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {

      Map<String, Object> variables = new HashMap<>(arguments);
      User user = null;

      RenderResult result = RenderResult.OK;
      String message = null;
      boolean loggedin = false;
      if (operation.equals(CommandOperation.POST)) {

        message = INVALID_USERNAME_AND_OR_PASSWORD;
        String username = (String) arguments.get("username");
        String password = (String) arguments.get("password");

        user = getThothEnvironment().getUserManager().getUser(username);
        if (user != null) {

          boolean blocked = user.getBlockedUntil() != null && user.getBlockedUntil().getTime() > System.currentTimeMillis();
          if (blocked)
            message = getBlockedMessage(user);
          else
            loggedin = user.isValidPassword(password);

          ExpiringCache<String, Integer> loginFailCounters = getThothEnvironment().getLoginFailCounters();
          if (loggedin) {
            loginFailCounters.remove(username);
            message = null;
          } else {
            Integer counter = loginFailCounters.get(username);
            if (counter == null)
              counter = 0;
            counter++;
            loginFailCounters.put(username, counter);
            if (counter > MAX_LOGIN_ATTEMPTS) {
              Date blockedUntil = new Date(System.currentTimeMillis() + BLOCK_DURATION);
              LOG.warn("Somebody failed to login to account '" + username//
                  + "' for more than " + MAX_LOGIN_ATTEMPTS + " times. Account will be locked until " //
                  + getConfiguration().getTimestampFormat().format(blockedUntil));
              user.setBlockedUntil(blockedUntil);
              getThothEnvironment().getUserManager().updateUser(user);
              message = getBlockedMessage(user);
            }
          }
        }
      }
      variables.put("message", message);
      variables.put("loggedin", loggedin);
      if (!loggedin) {
        render(skin.getLoginTemplate(), contextName, arguments, variables, outputStream);
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

  protected String getBlockedMessage(User user) {
    return SORRY_YOUR_ACCOUNT_IS_LOCKED_UNTIL + getConfiguration().getTimestampFormat().format(user.getBlockedUntil());
  }
}
