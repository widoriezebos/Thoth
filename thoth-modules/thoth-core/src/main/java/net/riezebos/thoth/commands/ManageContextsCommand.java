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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.ContextManager;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.context.RepositoryType;
import net.riezebos.thoth.exceptions.ContextManagerException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;

public class ManageContextsCommand extends RendererBase implements Command {
  private static final Logger LOG = LoggerFactory.getLogger(ManageContextsCommand.class);

  public static final String CREATEREPOSITORY = "createrepository";
  public static final String UPDATEEPOSITORY = "updaterepository";
  public static final String DELETEREPOSITORY = "deleterepository";
  public static final String CREATECONTEXT = "createcontext";
  public static final String UPDATECONTEXT = "updatecontext";
  public static final String DELETECONTEXT = "deletecontext";

  public static final String ARG_NAME = "name";
  public static final String ARG_NEWNAME = "newname";
  public static final String ARG_TYPE = "type";
  public static final String ARG_BRANCH = "branch";
  public static final String ARG_LOCATION = "location";
  public static final String ARG_USERNAME = "username";
  public static final String ARG_PASSWORD = "password";

  public static final String ARG_REPOSITORYNAME = "repositoryname";
  public static final String ARG_LIBRARYROOT = "libraryroot";
  public static final String ARG_REFRESHINTERVAL = "refreshinterval";

  public static final String ARG_MESSAGE = "message";

  public ManageContextsCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "managecontexts";
  }

  @Override
  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {
      RenderResult result = RenderResult.OK;
      ContentManager contentManager = getContentManager(contextName);
      if (!contentManager.getAccessManager().hasPermission(identity, path, Permission.MANAGE_CONTEXTS))
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

  protected RenderResult handleRender(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws Exception {
    Map<String, Object> variables = new HashMap<>(arguments);

    ContextManager contextManager = getThothEnvironment().getContextManager();
    List<ContextDefinition> contexts = new ArrayList<>(contextManager.getContextDefinitions().values());
    List<RepositoryDefinition> repositories = new ArrayList<>(contextManager.getRepositoryDefinitions().values());
    Collections.sort(contexts);
    Collections.sort(repositories);

    variables.put("contexts", contexts);
    variables.put("repositories", repositories);

    render(skin.getManageContextsTemplate(), contextName, arguments, variables, outputStream);

    return RenderResult.OK;
  }

  protected RenderResult handleOperation(String operationCode, Identity identity, String contextName, String path, CommandOperation operation,
      Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws Exception {
    if (operationCode != null) {
      switch (operationCode) {
      case CREATEREPOSITORY:
        createRepository(arguments);
        break;
      case UPDATEEPOSITORY:
        updateRepository(arguments);
        break;
      case DELETEREPOSITORY:
        deleteRepository(arguments);
        break;
      case CREATECONTEXT:
        createContext(arguments);
        break;
      case UPDATECONTEXT:
        updateContext(arguments);
        break;
      case DELETECONTEXT:
        deleteContext(arguments);
        break;
      default:
        LOG.warn("Unsupported operation code: " + operationCode);
      }
    }
    return handleRender(identity, contextName, path, operation, arguments, skin, outputStream);
  }

  protected void createRepository(Map<String, Object> arguments) throws ContextManagerException {
    String name = (String) arguments.get(ARG_NAME);
    String type = (String) arguments.get(ARG_TYPE);
    String location = (String) arguments.get(ARG_LOCATION);
    String username = (String) arguments.get(ARG_USERNAME);
    String password = (String) arguments.get(ARG_PASSWORD);

    boolean valid = validateRepositoryArguments(arguments, true);

    if (valid) {
      ContextManager contextManager = getThothEnvironment().getContextManager();
      RepositoryDefinition repo = new RepositoryDefinition();
      repo.setName(name);
      repo.setType(RepositoryType.convert(type));
      repo.setLocation(location);
      repo.setUsername(username);
      repo.setPassword(password);
      contextManager.createRepositoryDefinition(repo);
      arguments.put(ARG_MESSAGE, "Repository definition " + name + " created");
    }
  }

  protected void updateRepository(Map<String, Object> arguments) throws ContextManagerException {
    String name = (String) arguments.get(ARG_NAME);
    String newname = (String) arguments.get(ARG_NEWNAME);
    String type = (String) arguments.get(ARG_TYPE);
    String location = (String) arguments.get(ARG_LOCATION);
    String username = (String) arguments.get(ARG_USERNAME);
    String password = (String) arguments.get(ARG_PASSWORD);

    boolean valid = validateRepositoryArguments(arguments, false);

    if (valid) {
      ContextManager contextManager = getThothEnvironment().getContextManager();
      RepositoryDefinition repositoryDefinition = contextManager.getRepositoryDefinition(name);
      if (repositoryDefinition != null) {
        if (StringUtils.isNotBlank(newname))
          repositoryDefinition.setName(newname);
        repositoryDefinition.setType(RepositoryType.convert(type));
        repositoryDefinition.setLocation(location);
        repositoryDefinition.setUsername(username);
        if (password != null)
          repositoryDefinition.setPassword(password);
        contextManager.updateRepositoryDefinition(repositoryDefinition);
        arguments.put(ARG_MESSAGE, "Repository definition " + name + " updated");
      }
    }
  }

  protected void deleteRepository(Map<String, Object> arguments) throws ContextManagerException, DatabaseException {
    String identifier = (String) arguments.get(ARG_NAME);

    ContextManager contextManager = getThothEnvironment().getContextManager();
    RepositoryDefinition repositoryDefinition = contextManager.getRepositoryDefinition(identifier);
    if (repositoryDefinition != null) {
      if (contextManager.isInUse(repositoryDefinition)) {
        arguments.put(ARG_MESSAGE, "Cannot delete repository " + repositoryDefinition.getName() + " because it is in use.");
      } else {
        contextManager.deleteRepositoryDefinition(repositoryDefinition);
        arguments.put(ARG_MESSAGE, "Deleted " + repositoryDefinition.getType() + " repository definition " + repositoryDefinition.getName());
      }
    } else {
      arguments.put(ARG_MESSAGE, "Repository with name '" + identifier + "' not found");
    }
  }

  protected boolean validateRepositoryArguments(Map<String, Object> arguments, boolean forCreation) throws ContextManagerException {

    boolean valid = true;

    String name = (String) arguments.get(ARG_NAME);
    String type = (String) arguments.get(ARG_TYPE);

    if (StringUtils.isBlank(type)) {
      arguments.put(ARG_MESSAGE, "Type cannot be blank");
      valid = false;
    } else if (StringUtils.isBlank(name)) {
      arguments.put(ARG_MESSAGE, "Name cannot be blank");
      valid = false;
    } else {
      ContextManager contextManager = getThothEnvironment().getContextManager();
      RepositoryDefinition existing = contextManager.getRepositoryDefinition(name);
      if (forCreation && existing != null) {
        arguments.put(ARG_MESSAGE, "Repository with name " + name + " already exists");
        valid = false;
      }
      if (!forCreation && existing == null) {
        arguments.put(ARG_MESSAGE, "Repository with name " + name + " not found");
        valid = false;
      }
    }

    return valid;
  }

  ////////////////////////////////////////////////////////////////////////////

  protected void createContext(Map<String, Object> arguments) throws ContextManagerException {
    String contextName = (String) arguments.get(ARG_NAME);
    String repositoryName = (String) arguments.get(ARG_REPOSITORYNAME);
    String branch = (String) arguments.get(ARG_BRANCH);
    String libraryRoot = (String) arguments.get(ARG_LIBRARYROOT);
    String refreshInterval = (String) arguments.get(ARG_REFRESHINTERVAL);
    if (StringUtils.isBlank(refreshInterval))
      refreshInterval = "0";

    boolean valid = validateContextArguments(arguments, true);

    if (valid) {
      long refreshIntervalSecs = Long.parseLong(refreshInterval);
      ContextManager contextManager = getThothEnvironment().getContextManager();
      RepositoryDefinition repositoryDefinition = contextManager.getRepositoryDefinition(repositoryName);
      ContextDefinition contextDefinition = new ContextDefinition(repositoryDefinition, contextName, branch, libraryRoot, refreshIntervalSecs);
      contextManager.createContextDefinition(contextDefinition);
      arguments.put(ARG_MESSAGE, "Context " + contextName + " created");
    }
  }

  protected void updateContext(Map<String, Object> arguments) throws ContextManagerException {
    String contextName = (String) arguments.get(ARG_NAME);
    String newContextName = (String) arguments.get(ARG_NEWNAME);
    String repositoryName = (String) arguments.get(ARG_REPOSITORYNAME);
    String branch = (String) arguments.get(ARG_BRANCH);
    String libraryRoot = (String) arguments.get(ARG_LIBRARYROOT);
    String refreshInterval = (String) arguments.get(ARG_REFRESHINTERVAL);
    if (StringUtils.isBlank(refreshInterval))
      refreshInterval = "0";

    boolean valid = validateContextArguments(arguments, false);

    if (valid) {
      long refreshIntervalSeconds = Long.parseLong(refreshInterval);
      ContextManager contextManager = getThothEnvironment().getContextManager();
      ContextDefinition contextDefinition = contextManager.getContextDefinition(contextName);
      if (contextDefinition != null) {
        RepositoryDefinition repositoryDefinition = contextManager.getRepositoryDefinition(repositoryName);
        if (StringUtils.isNotBlank(newContextName))
          contextDefinition.setName(newContextName);
        contextDefinition.setBranch(branch);
        contextDefinition.setLibraryRoot(libraryRoot);
        contextDefinition.setRefreshInterval(refreshIntervalSeconds);
        contextDefinition.setRepositoryDefinition(repositoryDefinition);
        contextManager.updateContextDefinition(contextDefinition);
        arguments.put(ARG_MESSAGE, "Context " + contextName + " updated");
      } else {
        arguments.put(ARG_MESSAGE, "Could not find context named " + contextName);
      }
    }
  }

  protected void deleteContext(Map<String, Object> arguments) throws ContextManagerException, DatabaseException {
    String contextName = (String) arguments.get(ARG_NAME);

    ContextManager contextManager = getThothEnvironment().getContextManager();
    ContextDefinition contextDefinition = contextManager.getContextDefinition(contextName);
    if (contextDefinition != null) {
      contextManager.deleteContextDefinition(contextDefinition);
      arguments.put(ARG_MESSAGE, "Deleted context " + contextName);
    } else {
      arguments.put(ARG_MESSAGE, "Context with name '" + contextName + "' not found");
    }
  }

  protected boolean validateContextArguments(Map<String, Object> arguments, boolean forCreation) throws ContextManagerException {

    boolean valid = true;
    String contextName = (String) arguments.get(ARG_NAME);
    String repositoryName = (String) arguments.get(ARG_REPOSITORYNAME);

    if (StringUtils.isBlank(repositoryName)) {
      arguments.put(ARG_MESSAGE, "Repository name cannot be blank");
      valid = false;
    } else if (StringUtils.isBlank(contextName)) {
      arguments.put(ARG_MESSAGE, "Name cannot be blank");
      valid = false;
    } else {
      ContextManager contextManager = getThothEnvironment().getContextManager();
      ContextDefinition existing = contextManager.getContextDefinitions().get(contextName.toLowerCase());
      if (forCreation && existing != null) {
        arguments.put(ARG_MESSAGE, "Context with name " + contextName + " already exists");
        valid = false;
      }
      if (!forCreation && existing == null) {
        arguments.put(ARG_MESSAGE, "Context with name " + contextName + " not found");
        valid = false;
      }
    }
    return valid;
  }

}
