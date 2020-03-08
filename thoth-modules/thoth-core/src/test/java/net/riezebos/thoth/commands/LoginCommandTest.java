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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.testutil.TestUserManager;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;

public class LoginCommandTest extends CommandTest {

  private boolean wrongPw = false;

  @Override
  protected Map<String, Object> getParameters(ContentManager contentManager, String path) throws SkinManagerException {
    Map<String, Object> parameters = super.getParameters(contentManager, path);
    parameters.put("username", "administrator");
    parameters.put("password", wrongPw ? "wrong" : TestUserManager.ADMINISTRATORPW);
    return parameters;
  }

  @Test
  public void test() throws IOException, ConfigurationException, ContextNotFoundException, ContentManagerException {

    String[] htmlExists = new String[] {"Please enter your credentials to login"};

    LoginCommand loginCommand = new LoginCommand(setupContentManager(), this);
    testCommand(loginCommand, "/", CommandOperation.GET, "login", htmlExists, null, null);

    htmlExists = new String[] {};
    RenderResult renderResult = testCommand(loginCommand, "/", CommandOperation.POST, "login", htmlExists, null, null);
    assertEquals(RenderResult.LOGGED_IN, renderResult);
    wrongPw = true;
    renderResult = testCommand(loginCommand, "/", CommandOperation.POST, "login", htmlExists, null, null);
    assertNotEquals(RenderResult.LOGGED_IN, renderResult);
    UserManager userManager = getThothEnvironment().getUserManager();
    User user = userManager.getUser("administrator");
    assertTrue(user.getBlockedUntil() == null);
    for (int i = 0; i < 10; i++) {
      renderResult = testCommand(loginCommand, "/", CommandOperation.POST, "login", htmlExists, null, null);
      assertNotEquals(RenderResult.LOGGED_IN, renderResult);
    }
    user = userManager.getUser("administrator");
    assertTrue(user.getBlockedUntil() != null);
  }

}
