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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;

public class ManageUsersCommand extends RendererBase implements Command {

  public ManageUsersCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "manageusers";
  }

  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {
      RenderResult result = RenderResult.OK;
      ContentManager contentManager = getContentManager(contextName);
      if (!contentManager.getAccessManager().hasPermission(identity, path, Permission.MANAGE_USERS))
        return RenderResult.FORBIDDEN;

      Map<String, Object> variables = new HashMap<>(arguments);

      UserManager userManager = getThothEnvironment().getUserManager();
      List<User> users = userManager.listUsers();
      List<Group> groups = userManager.listGroups();
      List<Identity> identities = new ArrayList<Identity>();
      identities.addAll(users);
      identities.addAll(groups);

      variables.put("users", users);
      variables.put("groups", groups);
      variables.put("identities", identities);

      if (asJson(arguments))
        executeJson(variables, outputStream);
      else {
        String manageUsersTemplate = skin.getManageUsersTemplate();
        renderTemplate(manageUsersTemplate, null, variables, outputStream);
      }
      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
