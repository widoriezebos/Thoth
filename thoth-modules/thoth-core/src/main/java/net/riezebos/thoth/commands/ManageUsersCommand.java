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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;

public class ManageUsersCommand extends RendererBase implements Command {
  private static final String OPERATION_ARGUMENT = "operation";
  private static final Logger LOG = LoggerFactory.getLogger(ManageUsersCommand.class);

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

      String operationCode = (String) arguments.get(OPERATION_ARGUMENT);
      if (StringUtils.isNotBlank(operationCode))
        result = handleOperation(operationCode, identity, contextName, path, operation, arguments, skin, outputStream);
      else
        result = handleRender(identity, contextName, path, operation, arguments, skin, outputStream);

      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  protected RenderResult handleOperation(String operationCode, Identity identity, String contextName, String path, CommandOperation operation,
      Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws Exception {
    if (operationCode != null) {
      switch (operationCode) {
      case "createuser":
        createUser(arguments);
        break;
      case "updateuser":
        updateUser(arguments);
        break;
      case "creategroup":
        createGroup(arguments);
        break;
      case "deleteidentity":
        deleteIdentity(arguments);
        break;
      case "grantpermission":
        grantPermission(arguments);
        break;
      case "revokepermission":
        revokePermission(arguments);
        break;
      case "addmember":
        addMember(arguments);
        break;
      case "removemember":
        removeMember(arguments);
        break;
      default:
        LOG.warn("Unsupported operation code: " + operationCode);
      }
    }
    return handleRender(identity, contextName, path, operation, arguments, skin, outputStream);
  }

  protected void addMember(Map<String, Object> arguments) throws UserManagerException {
    String groupIdentifier = (String) arguments.get("group");
    String memberIdentifier = (String) arguments.get("member");

    UserManager userManager = getThothEnvironment().getUserManager();
    Group group = userManager.getGroup(groupIdentifier);
    Identity member = userManager.getIdentity(memberIdentifier);
    if (group == null) {
      arguments.put("message", "Group with identifier '" + groupIdentifier + "' not found");
    } else if (member == null) {
      arguments.put("message", "Member with identifier '" + memberIdentifier + "' not found");
    } else {
      userManager.createMembership(group, member);
      arguments.put("message", "Member '" + memberIdentifier + "' added to group " + groupIdentifier);
    }
  }

  protected void removeMember(Map<String, Object> arguments) throws UserManagerException {
    String groupIdentifier = (String) arguments.get("group");
    String memberIdentifier = (String) arguments.get("member");

    UserManager userManager = getThothEnvironment().getUserManager();
    Group group = userManager.getGroup(groupIdentifier);
    Identity member = userManager.getIdentity(memberIdentifier);
    if (group == null) {
      arguments.put("message", "Group with identifier '" + groupIdentifier + "' not found");
    } else if (member == null) {
      arguments.put("message", "Member with identifier '" + memberIdentifier + "' not found");
    } else {
      userManager.deleteMembership(group, member);
      arguments.put("message", "Member '" + memberIdentifier + "' removed from group " + groupIdentifier);
    }
  }

  protected void revokePermission(Map<String, Object> arguments) throws UserManagerException {
    String identifier = (String) arguments.get("group");
    String permission = (String) arguments.get("permission");

    UserManager userManager = getThothEnvironment().getUserManager();
    Identity identity = userManager.getIdentity(identifier);
    if (identity instanceof Group) {
      Group group = (Group) identity;
      Permission perm = Permission.valueOf(permission);
      group.revokePermission(perm);
      userManager.updatePermissions(group);
      arguments.put("message", "Permissions updated. Revoked " + perm + " from " + identifier);
    } else
      arguments.put("message", "Group with identifier '" + identifier + "' not found");

  }

  protected void grantPermission(Map<String, Object> arguments) throws UserManagerException {
    String identifier = (String) arguments.get("group");
    String permission = (String) arguments.get("permission");

    UserManager userManager = getThothEnvironment().getUserManager();
    Identity identity = userManager.getIdentity(identifier);
    if (identity instanceof Group) {
      Group group = (Group) identity;
      Permission perm = Permission.valueOf(permission);
      group.grantPermission(perm);
      userManager.updatePermissions(group);
      arguments.put("message", "Permissions updated. Granted " + perm + " to " + identifier);
    } else
      arguments.put("message", "Group with identifier '" + identifier + "' not found");
  }

  protected void deleteIdentity(Map<String, Object> arguments) throws UserManagerException {
    String identifier = (String) arguments.get("identifier");

    UserManager userManager = getThothEnvironment().getUserManager();
    Identity identity = userManager.getIdentity(identifier);
    if (identity != null) {
      if (identity.isAdministrator()) {
        arguments.put("message", "Cannot delete the administrator");
      } else {
        userManager.deleteIdentity(identity);
        arguments.put("message", "Deleted " + identity.getTypeName() + " " + identity.getIdentifier());
      }
    } else {
      arguments.put("message", "Identity with identifier '" + identifier + "' not found");
    }
  }

  protected void createUser(Map<String, Object> arguments) throws UserManagerException {
    String identifier = (String) arguments.get("identifier");
    String firstname = (String) arguments.get("firstname");
    String lastname = (String) arguments.get("lastname");
    String password = (String) arguments.get("password");

    boolean valid = validateIdentity(identifier, arguments);
    if (StringUtils.isBlank(password)) {
      arguments.put("message", "Password cannot be blank");
      valid = false;
    }

    if (valid) {
      UserManager userManager = getThothEnvironment().getUserManager();
      User user = new User(identifier);
      user.setFirstname(firstname);
      user.setLastname(lastname);
      user.setPassword(password);
      userManager.createUser(user);
      arguments.put("message", "User " + identifier + " created");
    }
  }

  protected void updateUser(Map<String, Object> arguments) throws UserManagerException, ParseException {
    String identifier = (String) arguments.get("identifier");
    String firstname = (String) arguments.get("firstname");
    String lastname = (String) arguments.get("lastname");
    String password = (String) arguments.get("password");
    String blocked = (String) arguments.get("blocked");

    UserManager userManager = getThothEnvironment().getUserManager();
    User user = userManager.getUser(identifier);
    if (user == null) {
      arguments.put("message", "User with identifier " + identifier + " not found");
    } else {
      user.setFirstname(firstname);
      user.setLastname(lastname);
      if (StringUtils.isNotBlank(password))
        user.setPassword(password);
      if (StringUtils.isBlank(blocked))
        user.setBlockedUntil(null);
      else {
        if (!user.isAdministrator())
          user.setBlockedUntil(getConfiguration().getDateFormat().parse("01-01-2200"));
        else {
          LOG.warn("Refuse to block administrator because could potentially lead to total lockdown of Thoth");
        }
      }
      user.setPassword(password);
      userManager.updateUser(user);
      arguments.put("message", "User " + identifier + " updated");
    }
  }

  protected void createGroup(Map<String, Object> arguments) throws UserManagerException {
    String identifier = (String) arguments.get("identifier");
    boolean valid = validateIdentity(identifier, arguments);

    if (valid) {
      UserManager userManager = getThothEnvironment().getUserManager();
      Group group = new Group(identifier);
      userManager.createGroup(group);
      arguments.put("message", "Group " + identifier + " created");
    }
  }

  protected boolean validateIdentity(String identifier, Map<String, Object> arguments) throws UserManagerException {
    boolean valid = true;
    if (StringUtils.isBlank(identifier)) {
      arguments.put("message", "Identifier cannot be blank");
      valid = false;
    }
    UserManager userManager = getThothEnvironment().getUserManager();
    Identity identity = userManager.getIdentity(identifier);
    if (identity != null) {
      arguments.put("message", "There is already a " + identity.getTypeName() + " with identifier " + identifier);
      valid = false;
    }
    return valid;
  }

  protected RenderResult handleRender(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws Exception {
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
    return RenderResult.OK;
  }
}
