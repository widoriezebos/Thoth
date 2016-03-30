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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.context.RepositoryType;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.util.PropertyLoader;

public abstract class ConfigurationBase extends PropertyLoader implements Configuration {

  private Map<String, RepositoryDefinition> repositoryDefinitions = new HashMap<>();
  private Map<String, ContextDefinition> contextDefinitions = new HashMap<>();

  public ConfigurationBase() {
  }

  @Override
  public Configuration clone() {
    try {
      ConfigurationBase clone = (ConfigurationBase) super.clone();
      clone.repositoryDefinitions = new HashMap<>();
      for (Entry<String, RepositoryDefinition> entry : repositoryDefinitions.entrySet()) {
        clone.repositoryDefinitions.put(entry.getKey(), entry.getValue().clone());
      }
      clone.contextDefinitions = new HashMap<>();
      for (Entry<String, ContextDefinition> entry : contextDefinitions.entrySet()) {
        clone.contextDefinitions.put(entry.getKey(), entry.getValue().clone());
      }
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  protected void clear() {
    super.clear();
    repositoryDefinitions = new HashMap<>();
    contextDefinitions = new HashMap<>();
  }

  @Override
  public Map<String, ContextDefinition> getConfiguredContextDefinitions() {
    return contextDefinitions;
  }

  @Override
  public Map<String, RepositoryDefinition> getConfiguredRepositoryDefinitions() {
    return repositoryDefinitions;
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
        String branch = getValue("context." + idx + ".branch", null);
        String library = getValue("context." + idx + ".library", null);
        String refreshSeconds = getValue("context." + idx + ".refreshseconds", "60");
        long refreshSecs = Long.parseLong(refreshSeconds);

        if (repository == null)
          throw new ConfigurationException("Repository setting not correct for context." + idx + ".repository");

        RepositoryDefinition repositoryDefinition = repositoryDefinitions.get(repository.toLowerCase());
        if (repositoryDefinition == null)
          throw new ConfigurationException("Context " + contextName + " references undefined Repository '" + repository + "'");
        ContextDefinition contextdef = new ContextDefinition(repositoryDefinition, contextName, branch, library, refreshSecs);
        contextdef.setImmutable(true);

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
        // Do some backwards compatibility (support the old name URL which is now renamed to location)
        // Will be removed in the future (then only location will be supported)
        String url = getValue("repository." + idx + ".url", null);
        String location = getValue("repository." + idx + ".location", null);
        repodef.setLocation(location == null ? url : location);
        repodef.setUsername(getValue("repository." + idx + ".username", null));
        repodef.setPassword(getValue("repository." + idx + ".password", null));
        repodef.setType(RepositoryType.convert(getValue("repository." + idx + ".type")));
        repodef.setImmutable(true);
        String key = repodef.getName().toLowerCase();
        if (repositoryDefinitions.containsKey(key)) {
          throw new ConfigurationException("Repository name not unique (case insensitive by the way): " + key);
        }
        repositoryDefinitions.put(key, repodef);
        doneOne = true;
      }
    } while (doneOne);
  }

  @Override
  public void discard() {

  }
}
