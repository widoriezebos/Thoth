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
package net.riezebos.thoth.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.util.PropertyLoader;

public abstract class ConfigurationBase extends PropertyLoader implements Configuration {

  private Map<String, RepositoryDefinition> repositoryDefinitions = new HashMap<>();
  private Map<String, ContextDefinition> contextDefinitions = new HashMap<>();

  public ConfigurationBase() {
  }

  @Override
  public Map<String, ContextDefinition> getContextDefinitions() {
    return contextDefinitions;
  }

  @Override
  public Map<String, RepositoryDefinition> getRepositoryDefinitions() {
    return repositoryDefinitions;
  }

  @Override
  public ContextDefinition getContextDefinition(String name) throws ContextNotFoundException {
    ContextDefinition contextDefinition = contextDefinitions.get(name.toLowerCase());
    if (contextDefinition == null)
      throw new ContextNotFoundException(name);
    return contextDefinition;
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getContexts()
   */
  @Override
  public List<String> getContexts() {
    List<String> result = new ArrayList<>();
    for (ContextDefinition def : getContextDefinitions().values())
      result.add(def.getName());
    Collections.sort(result);
    return result;
  }

  protected void loadContextDefinitions() throws ConfigurationException {
    int idx = 0;
    boolean doneOne;
    do {
      idx++;
      doneOne = false;
      String contextName = getValue("context." + idx + ".name", null);
      if (StringUtils.isNotBlank(contextName)) {

        String repository = getValue("context." + idx + ".repository");
        String branch = getValue("context." + idx + ".branch");
        String refreshSeconds = getValue("context." + idx + ".refreshseconds", "60");
        long refreshMs = Long.parseLong(refreshSeconds) * 1000;

        RepositoryDefinition repositoryDefinition = repositoryDefinitions.get(repository.toLowerCase());
        if (repositoryDefinition == null)
          throw new ConfigurationException("Context " + contextName + " references undefined Repository '" + repository + "'");
        ContextDefinition contextdef = new ContextDefinition(repositoryDefinition, contextName, branch, refreshMs);

        String key = contextdef.getName().toLowerCase();
        if (contextDefinitions.containsKey(key)) {
          throw new ConfigurationException("Context name not unique (case insensitive by the way): " + key);
        }
        contextDefinitions.put(key, contextdef);
        doneOne = true;
      }
    } while (doneOne);

  }

  protected void loadRepositoryDefinitions() throws ConfigurationException {
    int idx = 0;
    boolean doneOne;
    do {
      idx++;
      doneOne = false;
      String repositoryName = getValue("repository." + idx + ".name", null);
      if (StringUtils.isNotBlank(repositoryName)) {
        RepositoryDefinition repodef = new RepositoryDefinition();
        repodef.setName(repositoryName);
        repodef.setUrl(getValue("repository." + idx + ".url"));
        repodef.setUsername(getValue("repository." + idx + ".username"));
        repodef.setPassword(getValue("repository." + idx + ".password"));
        repodef.setType(getValue("repository." + idx + ".type"));
        String key = repodef.getName().toLowerCase();
        if (repositoryDefinitions.containsKey(key)) {
          throw new ConfigurationException("Repository name not unique (case insensitive by the way): " + key);
        }
        repositoryDefinitions.put(key, repodef);
        doneOne = true;
      }
    } while (doneOne);
  }
}