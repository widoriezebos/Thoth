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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ConfigurationFactory;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.configuration.RepositoryDefinition;
import net.riezebos.thoth.content.impl.FSContentManager;
import net.riezebos.thoth.content.impl.GitContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class ContentManagerFactory {

  private static Map<String, ContentManager> managers = new HashMap<>();

  public static ContentManager getContentManager(String context) throws ContentManagerException {

    Configuration configuration = ConfigurationFactory.getConfiguration();
    ContextDefinition contextDefinition = configuration.getContextDefinition(context);

    ContentManager contentManager;
    synchronized (managers) {
      contentManager = managers.get(contextDefinition.getName());
    }

    if (contentManager == null) {
      synchronized (managers) {
        RepositoryDefinition repositoryDefinition = contextDefinition.getRepositoryDefinition();
        String type = repositoryDefinition.getType();
        if ("git".equalsIgnoreCase(type))
          contentManager = new GitContentManager(contextDefinition);
        else if ("fs".equalsIgnoreCase(type))
          contentManager = new FSContentManager(contextDefinition);
        else
          throw new ContentManagerException("Unsupported version control type: " + type);
        contentManager.refresh();
        contentManager.enableAutoRefresh();
        managers.put(contextDefinition.getName(), contentManager);
      }
    }

    return contentManager;
  }

  /**
   * Returns a list of Contexts as defined by the Configuration
   * 
   * @return
   */
  public static List<String> getContexts() {
    Configuration configuration = ConfigurationFactory.getConfiguration();
    return configuration.getContexts();
  }

  // Touches all the contexts. Can be used to warm up a server
  public static void touch() throws ContentManagerException {
    Configuration configuration = ConfigurationFactory.getConfiguration();
    for (String context : configuration.getContexts())
      getContentManager(context);
  }

  public static String getRefreshTimestamp(String contextName) throws ContentManagerException {
    Configuration configuration = ConfigurationFactory.getConfiguration();
    SimpleDateFormat dateFormat = configuration.getTimestampFormat();
    Date latestRefresh = new Date(0L);
    for (String context : configuration.getContexts()) {
      ContentManager contentManager = getContentManager(context);
      Date refresh = contentManager.getLatestRefresh();
      if (refresh != null && latestRefresh.compareTo(refresh) < 0)
        latestRefresh = refresh;
    }
    String refresh = latestRefresh == null ? "Never" : dateFormat.format(latestRefresh);
    return refresh;
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
