package net.riezebos.thoth.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;

public class ManageUsersCommandTest extends CommandTest {

  @Test
  public void test()
      throws ContextNotFoundException, SkinManagerException, RenderException, UnsupportedEncodingException, ContentManagerException, IOException {

    ThothEnvironment thothEnvironment = setupContentManager();
    UserManager userManager = thothEnvironment.getUserManager();
    ManageUsersCommand manageUsersCommand = new ManageUsersCommand(thothEnvironment, this);
    Map<String, String> args = new HashMap<String, String>();
    RenderResult renderResult = testCommand(manageUsersCommand, "/", CommandOperation.GET, "manageusers", null, null, args);
    assertEquals(RenderResult.OK, renderResult);

    String[] jsonExists = new String[] {""};

    // CREATE USER
    args = new HashMap<String, String>();
    args.put(ManageUsersCommand.OPERATION_ARGUMENT, ManageUsersCommand.CREATEUSER);
    args.put(ManageUsersCommand.ARG_IDENTIFIER, "testuser");
    args.put(ManageUsersCommand.ARG_FIRSTNAME, "TestFirst");
    args.put(ManageUsersCommand.ARG_LASTNAME, "TestLast");
    args.put(ManageUsersCommand.ARG_PASSWORD, "Password");
    renderResult = testCommand(manageUsersCommand, "/", CommandOperation.POST, "manageusers", null, jsonExists, args);

    User user = userManager.getUser("testuser");
    assertEquals("TestFirst", user.getFirstname());
    assertEquals("TestLast", user.getLastname());
    assertTrue(user.isValidPassword("Password"));

    // UPDATE USER
    args = new HashMap<String, String>();
    args.put(ManageUsersCommand.OPERATION_ARGUMENT, ManageUsersCommand.UPDATEUSER);
    args.put(ManageUsersCommand.ARG_IDENTIFIER, "testuser");
    args.put(ManageUsersCommand.ARG_FIRSTNAME, "TestFirst2");
    args.put(ManageUsersCommand.ARG_LASTNAME, "TestLast2");
    args.put(ManageUsersCommand.ARG_PASSWORD, "Password2");
    renderResult = testCommand(manageUsersCommand, "/", CommandOperation.POST, "manageusers", null, jsonExists, args);

    user = userManager.getUser("testuser");
    assertEquals("TestFirst2", user.getFirstname());
    assertEquals("TestLast2", user.getLastname());
    assertTrue(user.isValidPassword("Password2"));

    // CREATE GROUP
    args = new HashMap<String, String>();
    args.put(ManageUsersCommand.OPERATION_ARGUMENT, ManageUsersCommand.CREATEGROUP);
    args.put(ManageUsersCommand.ARG_IDENTIFIER, "testgroup");
    renderResult = testCommand(manageUsersCommand, "/", CommandOperation.POST, "manageusers", null, jsonExists, args);
    Group group = userManager.getGroup("testgroup");
    assertNotNull(group);

    // CREATE MEMBERSHIP
    args = new HashMap<String, String>();
    args.put(ManageUsersCommand.OPERATION_ARGUMENT, ManageUsersCommand.ADDMEMBER);
    args.put(ManageUsersCommand.ARG_GROUP, "testgroup");
    args.put(ManageUsersCommand.ARG_MEMBER, "testuser");
    renderResult = testCommand(manageUsersCommand, "/", CommandOperation.POST, "manageusers", null, jsonExists, args);
    group = userManager.getGroup("testgroup");
    assertNotNull(group);
    assertTrue(group.getMembers().contains(user));

    // GRANT PERMISSION
    args = new HashMap<String, String>();
    args.put(ManageUsersCommand.OPERATION_ARGUMENT, ManageUsersCommand.GRANTPERMISSION);
    args.put(ManageUsersCommand.ARG_GROUP, "testgroup");
    args.put(ManageUsersCommand.ARG_PERMISSION, Permission.PULL.toString());
    renderResult = testCommand(manageUsersCommand, "/", CommandOperation.POST, "manageusers", null, jsonExists, args);
    group = userManager.getGroup("testgroup");
    user = userManager.getUser("testuser");
    assertTrue(group.getPermissions().contains(Permission.PULL));

    // REVOKE PERMISSION
    args = new HashMap<String, String>();
    args.put(ManageUsersCommand.OPERATION_ARGUMENT, ManageUsersCommand.REVOKEPERMISSION);
    args.put(ManageUsersCommand.ARG_GROUP, "testgroup");
    args.put(ManageUsersCommand.ARG_PERMISSION, Permission.PULL.toString());
    renderResult = testCommand(manageUsersCommand, "/", CommandOperation.POST, "manageusers", null, jsonExists, args);
    group = userManager.getGroup("testgroup");
    user = userManager.getUser("testuser");
    assertFalse(group.getPermissions().contains(Permission.PULL));

    // DELETE MEMBERSHIP
    args = new HashMap<String, String>();
    args.put(ManageUsersCommand.OPERATION_ARGUMENT, ManageUsersCommand.REMOVEMEMBER);
    args.put(ManageUsersCommand.ARG_GROUP, "testgroup");
    args.put(ManageUsersCommand.ARG_MEMBER, "testuser");
    renderResult = testCommand(manageUsersCommand, "/", CommandOperation.POST, "manageusers", null, jsonExists, args);
    group = userManager.getGroup("testgroup");
    assertNotNull(group);
    assertFalse(group.getMembers().contains(user));

    // DELETE USER
    args = new HashMap<String, String>();
    args.put(ManageUsersCommand.OPERATION_ARGUMENT, ManageUsersCommand.DELETEIDENTITY);
    args.put(ManageUsersCommand.ARG_IDENTIFIER, "testuser");
    renderResult = testCommand(manageUsersCommand, "/", CommandOperation.POST, "manageusers", null, jsonExists, args);
    user = userManager.getUser("testuser");
    assertNull(user);

    // DELETE GROUP
    args = new HashMap<String, String>();
    args.put(ManageUsersCommand.OPERATION_ARGUMENT, ManageUsersCommand.DELETEIDENTITY);
    args.put(ManageUsersCommand.ARG_IDENTIFIER, "testgroup");
    renderResult = testCommand(manageUsersCommand, "/", CommandOperation.POST, "manageusers", null, jsonExists, args);
    group = userManager.getGroup("testgroup");
    assertNull(group);
  }

}
