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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.ContextManager;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.context.RepositoryType;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.renderers.RenderResult;

public class ManageContextsCommandTest extends CommandTest {

  @Test
  public void test()
      throws ContextNotFoundException, SkinManagerException, RenderException, UnsupportedEncodingException, ContentManagerException, IOException {

    ThothEnvironment thothEnvironment = setupContentManager();
    ContextManager contextManager = thothEnvironment.getContextManager();
    ManageContextsCommand manageContextsCommand = new ManageContextsCommand(thothEnvironment, this);
    Map<String, String> args = new HashMap<String, String>();
    RenderResult renderResult = testCommand(manageContextsCommand, "/", CommandOperation.GET, "managecontexts", null, null, args);
    assertEquals(RenderResult.OK, renderResult);

    String[] jsonExists = new String[] {""};

    // CREATEREPOSITORY
    args = new HashMap<String, String>();
    args.put(ManageContextsCommand.OPERATION_ARGUMENT, ManageContextsCommand.CREATEREPOSITORY);

    args.put(ManageContextsCommand.ARG_NAME, "testrepos");
    args.put(ManageContextsCommand.ARG_TYPE, "nop");
    args.put(ManageContextsCommand.ARG_LOCATION, "somelocation");
    args.put(ManageContextsCommand.ARG_USERNAME, "username");
    args.put(ManageContextsCommand.ARG_PASSWORD, "password");
    renderResult = testCommand(manageContextsCommand, "/", CommandOperation.POST, "managecontexts", null, jsonExists, args);

    RepositoryDefinition repositoryDefinition = contextManager.getRepositoryDefinition("testrepos");
    assertEquals("somelocation", repositoryDefinition.getLocation());
    assertEquals(RepositoryType.NOP, repositoryDefinition.getType());
    assertEquals("username", repositoryDefinition.getUsername());
    assertEquals("password", repositoryDefinition.getPassword());

    // UPDATEEPOSITORY
    args = new HashMap<String, String>();
    args.put(ManageContextsCommand.OPERATION_ARGUMENT, ManageContextsCommand.UPDATEEPOSITORY);

    args.put(ManageContextsCommand.ARG_NAME, "testrepos");
    args.put(ManageContextsCommand.ARG_NEWNAME, "testreposnew");
    args.put(ManageContextsCommand.ARG_TYPE, "git");
    args.put(ManageContextsCommand.ARG_LOCATION, "somelocationnew");
    args.put(ManageContextsCommand.ARG_USERNAME, "usernamenew");
    args.put(ManageContextsCommand.ARG_PASSWORD, "passwordnew");
    renderResult = testCommand(manageContextsCommand, "/", CommandOperation.POST, "managecontexts", null, jsonExists, args);

    repositoryDefinition = contextManager.getRepositoryDefinition("testreposnew");
    assertEquals("somelocationnew", repositoryDefinition.getLocation());
    assertEquals(RepositoryType.GIT, repositoryDefinition.getType());
    assertEquals("usernamenew", repositoryDefinition.getUsername());
    assertEquals("passwordnew", repositoryDefinition.getPassword());

    // CREATECONTEXT
    args = new HashMap<String, String>();
    args.put(ManageContextsCommand.OPERATION_ARGUMENT, ManageContextsCommand.CREATECONTEXT);

    args.put(ManageContextsCommand.ARG_NAME, "testcontext");
    args.put(ManageContextsCommand.ARG_REPOSITORYNAME, "testreposnew");
    args.put(ManageContextsCommand.ARG_BRANCH, "branch");
    args.put(ManageContextsCommand.ARG_LIBRARYROOT, "/");
    args.put(ManageContextsCommand.ARG_REFRESHINTERVAL, "60");
    renderResult = testCommand(manageContextsCommand, "/", CommandOperation.POST, "managecontexts", null, jsonExists, args);

    ContextDefinition contextDefinition = contextManager.getContextDefinition("testcontext");
    assertEquals("branch", contextDefinition.getBranch());
    assertEquals("/", contextDefinition.getLibraryRoot());
    assertEquals(60L, contextDefinition.getRefreshInterval());
    assertEquals("testreposnew", contextDefinition.getRepositoryName());

    // UPDATECONTEXT
    args = new HashMap<String, String>();
    args.put(ManageContextsCommand.OPERATION_ARGUMENT, ManageContextsCommand.UPDATECONTEXT);

    args.put(ManageContextsCommand.ARG_NAME, "testcontext");
    args.put(ManageContextsCommand.ARG_REPOSITORYNAME, "testreposnew");
    args.put(ManageContextsCommand.ARG_BRANCH, "branchnew");
    args.put(ManageContextsCommand.ARG_LIBRARYROOT, "/new");
    args.put(ManageContextsCommand.ARG_REFRESHINTERVAL, "90");
    renderResult = testCommand(manageContextsCommand, "/", CommandOperation.POST, "managecontexts", null, jsonExists, args);

    contextDefinition = contextManager.getContextDefinition("testcontext");
    assertEquals("branchnew", contextDefinition.getBranch());
    assertEquals("/new", contextDefinition.getLibraryRoot());
    assertEquals(90L, contextDefinition.getRefreshInterval());
    assertEquals("testreposnew", contextDefinition.getRepositoryName());

    // DELETECONTEXT
    Map<String, ContextDefinition> contextDefinitions = contextManager.getContextDefinitions();
    assertEquals(true, contextDefinitions.containsKey("testcontext"));

    args = new HashMap<String, String>();
    args.put(ManageContextsCommand.OPERATION_ARGUMENT, ManageContextsCommand.DELETECONTEXT);
    args.put(ManageContextsCommand.ARG_NAME, "testcontext");
    renderResult = testCommand(manageContextsCommand, "/", CommandOperation.POST, "managecontexts", null, jsonExists, args);
    contextDefinitions = contextManager.getContextDefinitions();
    assertEquals(false, contextDefinitions.containsKey("testcontext"));

    // DELETEEPOSITORY
    Map<String, RepositoryDefinition> repositoryDefinitions = contextManager.getRepositoryDefinitions();
    assertEquals(true, repositoryDefinitions.containsKey("testreposnew"));

    args = new HashMap<String, String>();
    args.put(ManageContextsCommand.OPERATION_ARGUMENT, ManageContextsCommand.DELETEREPOSITORY);
    args.put(ManageContextsCommand.ARG_NAME, "testreposnew");
    renderResult = testCommand(manageContextsCommand, "/", CommandOperation.POST, "managecontexts", null, jsonExists, args);
    repositoryDefinitions = contextManager.getRepositoryDefinitions();
    assertEquals(false, repositoryDefinitions.containsKey("testreposnew"));

  }

}
