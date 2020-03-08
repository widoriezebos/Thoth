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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;

public class UserProfileCommandTest extends CommandTest {

  private boolean loggedIn = true;

  @Override
  protected Map<String, Object> getParameters(ContentManager contentManager, String path) throws SkinManagerException {
    Map<String, Object> parameters = super.getParameters(contentManager, path);
    if (loggedIn) {
      parameters.put(Renderer.LOGGED_IN, Boolean.TRUE);
      parameters.put(Renderer.IDENTITY, "administrator");
    }
    return parameters;
  }

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    ThothEnvironment thothEnvironment = setupContentManager();
    UserManager userManager = thothEnvironment.getUserManager();
    UserProfileCommand userProfileCommand = new UserProfileCommand(thothEnvironment, this);
    Map<String, String> args = new HashMap<String, String>();
    RenderResult renderResult = testCommand(userProfileCommand, "/", CommandOperation.GET, "userprofile", null, null, args);
    assertEquals(RenderResult.OK, renderResult);

    String[] jsonExists = new String[] {""};

    // CREATE USER
    args = new HashMap<String, String>();
    args.put(UserProfileCommand.ARG_FIRSTNAME, "TestFirst");
    args.put(UserProfileCommand.ARG_LASTNAME, "TestLast");
    args.put(UserProfileCommand.ARG_PASSWORD, "Password");
    args.put(UserProfileCommand.ARG_PASSWORD2, "Password");
    renderResult = testCommand(userProfileCommand, "/", CommandOperation.POST, "userprofile", null, jsonExists, args);
    User user = userManager.getUser("administrator");
    assertEquals("TestFirst", user.getFirstname());
    assertEquals("TestLast", user.getLastname());
    assertTrue(user.isValidPassword("Password"));
  }

}
