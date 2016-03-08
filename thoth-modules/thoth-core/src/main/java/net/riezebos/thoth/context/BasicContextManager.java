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
package net.riezebos.thoth.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.context.dao.ContextDefinitionDao;
import net.riezebos.thoth.exceptions.ContextManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.DatabaseException;

public class BasicContextManager implements ContextManager {

  private ThothEnvironment thothEnvironment;
  private ContextDefinition global;
  private ContextDefinitionDao contextDefinitionDao;

  public BasicContextManager(ThothEnvironment thothEnvironment) throws DatabaseException {
    this.thothEnvironment = thothEnvironment;

    RepositoryDefinition repoDef = new RepositoryDefinition();
    repoDef.setName("global-nop-repository");
    repoDef.setType("nop");
    global = new ContextDefinition(repoDef, "*global*", null, null, 0);
    contextDefinitionDao = new ContextDefinitionDao(thothEnvironment.getThothDB());
  }

  @Override
  public Map<String, RepositoryDefinition> getRepositoryDefinitions() throws ContextManagerException {
    try {
      Map<String, RepositoryDefinition> configuredRepositoryDefinitions = thothEnvironment.getConfiguration().getConfiguredRepositoryDefinitions();
      Map<String, RepositoryDefinition> repositoryDefinitions = contextDefinitionDao.getRepositoryDefinitions();
      Map<String, RepositoryDefinition> result = new HashMap<>(repositoryDefinitions);
      result.putAll(configuredRepositoryDefinitions);
      return result;
    } catch (Exception e) {
      throw new ContextManagerException(e.getMessage(), e);
    }
  }

  @Override
  public Map<String, ContextDefinition> getContextDefinitions() throws ContextManagerException {
    try {
      Map<String, ContextDefinition> configuredContextDefinitions = thothEnvironment.getConfiguration().getConfiguredContextDefinitions();
      Map<String, ContextDefinition> contextDefinitions = contextDefinitionDao.getContextDefinitions();

      Map<String, ContextDefinition> result = new HashMap<>(contextDefinitions);
      result.putAll(configuredContextDefinitions);

      return result;
    } catch (Exception e) {
      throw new ContextManagerException(e.getMessage(), e);
    }
  }

  @Override
  public ContextDefinition getContextDefinition(String name) throws ContextNotFoundException, ContextManagerException {
    if (name != null) {
      name = name.toLowerCase();
      ContextDefinition contextDefinition = getContextDefinitions().get(name);
      if (contextDefinition == null)
        throw new ContextNotFoundException(name);
      return contextDefinition;
    } else
      return global;
  }

  @Override
  public List<String> getContexts() throws ContextManagerException {
    List<String> result = new ArrayList<>();
    for (ContextDefinition def : getContextDefinitions().values())
      result.add(def.getName());
    Collections.sort(result);
    return result;
  }

  @Override
  public ContextDefinition createContextDefinition(ContextDefinition contextDefinition) throws ContextManagerException {
    return contextDefinitionDao.createContextDefinition(contextDefinition);
  }

  @Override
  public boolean updateContextDefinition(ContextDefinition contextDefinition) throws ContextManagerException {
    return contextDefinitionDao.updateContextDefinition(contextDefinition);
  }

  @Override
  public void deleteContextDefinition(ContextDefinition contextDefinition) throws ContextManagerException {
    contextDefinitionDao.deleteContextDefinition(contextDefinition);
  }

  @Override
  public RepositoryDefinition getRepositoryDefinition(String repositoryName) throws ContextManagerException {
    return getRepositoryDefinitions().get(repositoryName.toLowerCase());
  }

  @Override
  public RepositoryDefinition createRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    return contextDefinitionDao.createRepositoryDefinition(repositoryDefinition);
  }

  @Override
  public boolean updateRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    return contextDefinitionDao.updateRepositoryDefinition(repositoryDefinition);
  }

  @Override
  public boolean deleteRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    return contextDefinitionDao.deleteRepositoryDefinition(repositoryDefinition);
  }

  @Override
  public boolean isInUse(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    return contextDefinitionDao.isInUse(repositoryDefinition);
  }

}
