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

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.util.PropertyLoader;

public abstract class ConfigurationBase extends PropertyLoader implements Configuration {

  private static final String GLOBAL_SITE = "*global_site*";

  private Map<String, CacheManager> caches = new HashMap<>();
  private Map<String, RepositoryDefinition> repositoryDefinitions = new HashMap<>();
  private Map<String, ContextDefinition> contextDefinitions = new HashMap<>();
  private ContextDefinition global;

  public ConfigurationBase() {
    RepositoryDefinition repoDef = new RepositoryDefinition();
    repoDef.setName("global-nop-repository");
    repoDef.setType("nop");
    global = new ContextDefinition(repoDef, "*global*", null, 0);
  }

  public CacheManager getCacheManager(ContentManager contentManager) {
    String contextKey = getContextKey(contentManager);
    CacheManager cacheManager;
    synchronized (caches) {
      cacheManager = caches.get(contextKey);
    }
    if (cacheManager == null) {
      cacheManager = new CacheManager(contentManager);
      synchronized (caches) {
        caches.put(contextKey, cacheManager);
      }
    }
    return cacheManager;
  }

  protected String getContextKey(ContentManager contentManager) {
    String contextKey;
    if (contentManager == null)
      contextKey = GLOBAL_SITE;
    else
      contextKey = contentManager.getContextName().toLowerCase().trim();
    return contextKey;
  }

  public void expireCache(ContentManager contentManager) {
    String contextKey = getContextKey(contentManager);
    synchronized (caches) {
      caches.remove(contextKey);
      // We do not know where the global site is getting it's data from so expire that one as well just to be safe
      caches.remove(GLOBAL_SITE);
    }
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
    if (name != null) {
      name = name.toLowerCase();
      ContextDefinition contextDefinition = contextDefinitions.get(name);
      if (contextDefinition == null)
        throw new ContextNotFoundException(name);
      return contextDefinition;
    } else
      return global;
  }

  @Override
  public boolean isValidContext(String name) {
    return contextDefinitions.containsKey(name.toLowerCase());
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
        String branch = getValue("context." + idx + ".branch", null);
        String refreshSeconds = getValue("context." + idx + ".refreshseconds", "60");
        long refreshMs = Long.parseLong(refreshSeconds) * 1000;

        if (repository == null)
          throw new ConfigurationException("Repository setting not correct for context." + idx + ".repository");

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
        // Do some backwards compatibility (support the old name URL which is now renamed to location)
        // Will be removed in the future (then only location will be supported)
        String url = getValue("repository." + idx + ".url", null);
        String location = getValue("repository." + idx + ".location", null);
        repodef.setLocation(location == null ? url : location);
        repodef.setUsername(getValue("repository." + idx + ".username", null));
        repodef.setPassword(getValue("repository." + idx + ".password", null));
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
