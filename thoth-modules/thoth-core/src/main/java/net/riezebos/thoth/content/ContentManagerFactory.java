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
package net.riezebos.thoth.content;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ConfigurationFactory;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.configuration.RepositoryDefinition;
import net.riezebos.thoth.content.impl.FSContentManager;
import net.riezebos.thoth.content.impl.GitContentManager;
import net.riezebos.thoth.content.impl.NopContentManager;
import net.riezebos.thoth.content.impl.ZipContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class ContentManagerFactory {

  private static Map<String, ContentManager> managers = new HashMap<>();

  public static ContentManager getContentManager(String contextName) throws ContentManagerException {

    ContentManager contentManager;
    synchronized (managers) {
      contentManager = managers.get(contextName);

      if (contentManager == null) {

        if (StringUtils.isBlank(contextName)) {
          RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
          repositoryDefinition.setType("nop");
          contentManager = registerContentManager(new NopContentManager(new ContextDefinition(repositoryDefinition, "", "", 0), null));
        } else {
          Configuration configuration = ConfigurationFactory.getConfiguration();
          ContextDefinition contextDefinition = configuration.getContextDefinition(contextName);
          RepositoryDefinition repositoryDefinition = contextDefinition.getRepositoryDefinition();

          String type = repositoryDefinition.getType();
          if ("git".equalsIgnoreCase(type))
            contentManager = registerContentManager(new GitContentManager(contextDefinition, configuration));
          else if ("fs".equalsIgnoreCase(type) || "filesystem".equalsIgnoreCase(type))
            contentManager = registerContentManager(new FSContentManager(contextDefinition, configuration));
          else if ("nop".equalsIgnoreCase(type))
            contentManager = registerContentManager(new NopContentManager(contextDefinition, configuration));
          else if ("zip".equalsIgnoreCase(type) || "jar".equalsIgnoreCase(type))
            contentManager = registerContentManager(new ZipContentManager(contextDefinition, configuration));
          else
            throw new ContentManagerException("Unsupported version control type: " + type);
        }
      }
    }

    return contentManager;
  }

  public static ContentManager registerContentManager(ContentManager contentManager) throws ContentManagerException {
    String contextName = contentManager.getContextName();
    contentManager.enableAutoRefresh();

    synchronized (managers) {
      managers.put(contextName, contentManager);
    }
    return contentManager;
  }

  // Touches all the contexts. Can be used to warm up a server
  public static void touch() throws ContentManagerException {
    Configuration configuration = ConfigurationFactory.getConfiguration();
    for (String context : configuration.getContexts())
      getContentManager(context);
  }

  public static Date getRefreshTimestamp(String contextName) throws ContentManagerException {

    Date latestRefresh = new Date(0L);
    synchronized (managers) {
      for (ContentManager contentManager : managers.values()) {
        Date refresh = contentManager.getLatestRefresh();
        if (refresh != null && latestRefresh.compareTo(refresh) < 0)
          latestRefresh = refresh;
      }
    }
    return latestRefresh;
  }

  public static void shutDown() throws ContentManagerException {
    Configuration configuration = ConfigurationFactory.getConfiguration();
    for (String context : configuration.getContexts())
      getContentManager(context).disableAutoRefresh();
  }

  public static String pullAll() throws ContentManagerException {
    StringBuilder report = new StringBuilder();

    Configuration configuration = ConfigurationFactory.getConfiguration();
    for (String context : configuration.getContexts())
      report.append(getContentManager(context).refresh());
    return report.toString();
  }

  public static void reindexAll() throws ContentManagerException {
    Configuration configuration = ConfigurationFactory.getConfiguration();
    for (String context : configuration.getContexts())
      getContentManager(context).reindex();
  }
}
