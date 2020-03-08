/* Copyright (c) 2020 W.T.J. Riezebos
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

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.comments.CommentManager;
import net.riezebos.thoth.content.comments.dao.CommentDao;
import net.riezebos.thoth.content.impl.ClasspathContentManager;
import net.riezebos.thoth.content.impl.FSContentManager;
import net.riezebos.thoth.content.impl.GitContentManager;
import net.riezebos.thoth.content.impl.NopContentManager;
import net.riezebos.thoth.content.impl.ZipContentManager;
import net.riezebos.thoth.context.BasicContextManager;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.ContextManager;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.context.RepositoryType;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextManagerException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.BasicUserManager;
import net.riezebos.thoth.user.UserManager;
import net.riezebos.thoth.util.ExpiringCache;
import net.riezebos.thoth.util.FinalWrapper;

public class ThothEnvironment implements ConfigurationChangeListener {
  private static final int FIVE_MINUTES = 5 * 60 * 1000;
  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
  public static final String CONFIGKEY_DEPRECATED = "configuration";
  public static final String CONFIGKEY = "thoth_configuration";
  private static final String GLOBAL_SITE = "*global_site*";

  private Map<String, ContentManager> managers = new HashMap<>();
  private Configuration configuration = null;
  private List<RendererChangeListener> rendererChangeListeners = new ArrayList<>();

  private volatile static FinalWrapper<ThothEnvironment> sharedThothContextWrapper = null;

  private volatile FinalWrapper<ThothDB> thothDbWrapper = null;
  private volatile FinalWrapper<UserManager> userManagerWrapper = null;
  private volatile FinalWrapper<CommentManager> commentManagerWrapper = null;
  private volatile FinalWrapper<ContextManager> contextManagerWrapper = null;
  private volatile FinalWrapper<ExpiringCache<String, Integer>> expiringCacheWrapper = null;

  public ContentManager getContentManager(ContextDefinition contextDefinition) throws ContentManagerException {
    return getContentManager(contextDefinition.getName());
  }

  public ContentManager getContentManager(String contextName) throws ContentManagerException {

    ContentManager contentManager;
    synchronized (managers) {
      contentManager = managers.get(contextName);

      if (contentManager == null) {

        if (StringUtils.isBlank(contextName)) {
          RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
          repositoryDefinition.setType(RepositoryType.NOP);
          contentManager = registerContentManager(new NopContentManager(new ContextDefinition(repositoryDefinition, "", "", "", 0), this));
        } else {
          ContextDefinition contextDefinition = getContextManager().getContextDefinition(contextName);
          RepositoryDefinition repositoryDefinition = contextDefinition.getRepositoryDefinition();

          RepositoryType type = repositoryDefinition.getType();
          switch (type) {
          case GIT:
            contentManager = registerContentManager(new GitContentManager(contextDefinition, this));
            break;
          case FILESYSTEM:
            contentManager = registerContentManager(new FSContentManager(contextDefinition, this));
            break;
          case CLASSPATH:
            contentManager = registerContentManager(new ClasspathContentManager(contextDefinition, this));
            break;
          case NOP:
            contentManager = registerContentManager(new NopContentManager(contextDefinition, this));
            break;
          case ZIP:
            contentManager = registerContentManager(new ZipContentManager(contextDefinition, this));
            break;
          default:
            throw new ContentManagerException("Unsupported version control type: " + type);
          }
        }
      }
    }

    return contentManager;
  }

  public ContentManager registerContentManager(ContentManager contentManager) throws ContentManagerException {
    String contextName = contentManager.getContextName();
    contentManager.enableAutoRefresh();

    synchronized (managers) {
      managers.put(contextName, contentManager);
    }
    return contentManager;
  }

  @Override
  public void contextAdded(ContextDefinition contextDefinition) {
    try {
      // Load it to make sure we are ready to go:
      getContentManager(contextDefinition);
    } catch (ContentManagerException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  @Override
  public void contextRemoved(ContextDefinition contextDefinition) {
    synchronized (managers) {
      managers.remove(contextDefinition.getName());
    }
  }

  @Override
  public void renderersChanged() {
    for (RendererChangeListener listener : rendererChangeListeners)
      listener.rendererDefinitionChanged();
  }

  // Touches all the contexts. Can be used to warm up a server
  public void touch() throws ContentManagerException {
    for (String context : getContextManager().getContextNames())
      try {
        LOG.info("Touching " + context);
        getContentManager(context);
      } catch (ContentManagerException e) {
        LOG.error(e.getMessage(), e);
      }
  }

  public Date getRefreshTimestamp(String contextName) throws ContentManagerException {

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

  public void shutDown() throws ContentManagerException {
    for (String context : getContextManager().getContextNames())
      getContentManager(context).disableAutoRefresh();
    getThothDB().shutdown();
    getConfiguration().discard();
  }

  public String pullAll() throws ContentManagerException {
    StringBuilder report = new StringBuilder();

    for (String context : getContextManager().getContextNames())
      report.append(getContentManager(context).refresh() + "\n");
    return report.toString();
  }

  public void reindexAll() throws ContentManagerException {
    for (String context : getContextManager().getContextNames())
      getContentManager(context).reindex();
  }

  public Configuration getConfiguration() {
    try {
      if (configuration == null) {
        String sourceSpec = determinePropertyPath();
        if (sourceSpec == null) {
          String msg = "There is no configuration defined. Please set either environment or system property '" + CONFIGKEY + "' and restart";
          LOG.error(msg);
          throw new IllegalArgumentException(msg);
        } else {
          LOG.info("Using " + sourceSpec + " for configuration");
          ConfigurationBase propertyBasedConfiguration = new PropertyBasedConfiguration();
          propertyBasedConfiguration.setSourceSpec(sourceSpec);
          propertyBasedConfiguration.reload();
          setConfiguration(propertyBasedConfiguration);
        }
      }
      return configuration;
    } catch (ConfigurationException | FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public void setConfiguration(Configuration configuration) {
    if (this.configuration != null) {
      this.configuration.discard();
    }

    HotReloadableConfiguration hotReloadableConfiguration = new HotReloadableConfiguration(configuration);
    hotReloadableConfiguration.addConfigurationChangeListener(this);
    this.configuration = hotReloadableConfiguration;
  }

  public String determinePropertyPath() {
    String propertyPath = System.getProperty(CONFIGKEY);
    if (propertyPath == null)
      propertyPath = System.getenv(CONFIGKEY);
    if (propertyPath == null)
      propertyPath = System.getProperty(CONFIGKEY_DEPRECATED);
    if (propertyPath == null)
      propertyPath = System.getenv(CONFIGKEY_DEPRECATED);
    return propertyPath;
  }

  protected String getContextKey(ContentManager contentManager) {
    String contextKey;
    if (contentManager == null)
      contextKey = GLOBAL_SITE;
    else
      contextKey = contentManager.getContextName().toLowerCase().trim();
    return contextKey;
  }

  public void addRendererChangedListener(RendererChangeListener listener) {
    rendererChangeListeners.add(listener);
  }

  public void removeRendererChangedListener(RendererChangeListener listener) {
    rendererChangeListeners.remove(listener);
  }

  public static void registerSharedContext(ThothEnvironment thothEnvironment) {
    sharedThothContextWrapper = new FinalWrapper<ThothEnvironment>(thothEnvironment);
  }

  /**
   * This method is synchronized to make sure there will ever only be one shared ThothEnvironment See
   * http://shipilev.net/blog/2014/safe-public-construction/#_singletons_and_singleton_factories
   *
   * @return
   */
  public static ThothEnvironment getSharedThothContext() {
    FinalWrapper<ThothEnvironment> wrapper = sharedThothContextWrapper;
    if (wrapper == null) {
      synchronized (ThothEnvironment.class) {
        wrapper = sharedThothContextWrapper;
        if (wrapper == null)
          wrapper = new FinalWrapper<ThothEnvironment>(new ThothEnvironment());
        sharedThothContextWrapper = wrapper;
      }
    }
    return wrapper.value;
  }

  /**
   * This method is synchronized to make sure there will ever only be one ThothDB for this environment.
   *
   * @return
   * @throws SQLException
   */
  public ThothDB getThothDB() throws DatabaseException {
    FinalWrapper<ThothDB> wrapper = thothDbWrapper;
    if (wrapper == null) {
      synchronized (this) {
        wrapper = thothDbWrapper;
        if (wrapper == null) {
          wrapper = new FinalWrapper<ThothDB>(createThothDB());
        }
        thothDbWrapper = wrapper;
      }
    }
    return wrapper.value;
  }

  /**
   * This method is synchronized to make sure there will ever only be one ThothDB for this environment. See
   * http://shipilev.net/blog/2014/safe-public-construction/#_singletons_and_singleton_factories
   *
   * @return
   * @throws SQLException
   */
  public ExpiringCache<String, Integer> getLoginFailCounters() throws DatabaseException {
    FinalWrapper<ExpiringCache<String, Integer>> wrapper = expiringCacheWrapper;
    if (wrapper == null) {
      synchronized (this) {
        wrapper = expiringCacheWrapper;
        if (wrapper == null)
          wrapper = new FinalWrapper<ExpiringCache<String, Integer>>(new ExpiringCache<>(FIVE_MINUTES));
        expiringCacheWrapper = wrapper;
      }
    }
    return wrapper.value;
  }

  protected ThothDB createThothDB() throws DatabaseException {
    ThothDB thothDB = new ThothDB(this);
    thothDB.init();
    return thothDB;
  }

  /**
   * This method is synchronized to make sure there will ever only be one UserManager for this environment.
   *
   * @return
   * @throws SQLException
   */
  public UserManager getUserManager() throws UserManagerException {
    FinalWrapper<UserManager> wrapper = userManagerWrapper;
    if (wrapper == null) {
      synchronized (this) {
        wrapper = userManagerWrapper;
        if (wrapper == null) {
          wrapper = setUserManager(new BasicUserManager(this));
        }
      }
    }
    return wrapper.value;
  }

  public FinalWrapper<UserManager> setUserManager(UserManager userManager) {
    FinalWrapper<UserManager> result = new FinalWrapper<UserManager>(userManager);
    userManagerWrapper = result;
    return result;
  }

  /**
   * This method is synchronized to make sure there will ever only be one CommentManager for this environment.
   *
   * @return
   * @throws SQLException
   */
  public CommentManager getCommentManager() throws ContentManagerException {
    FinalWrapper<CommentManager> wrapper = commentManagerWrapper;
    if (wrapper == null) {
      synchronized (this) {
        wrapper = commentManagerWrapper;
        if (wrapper == null) {
          wrapper = setCommentManager(new CommentDao(getThothDB()));
        }
      }
    }
    return wrapper.value;
  }

  public FinalWrapper<CommentManager> setCommentManager(CommentManager commentManager) {
    FinalWrapper<CommentManager> result = new FinalWrapper<CommentManager>(commentManager);
    commentManagerWrapper = result;
    return result;
  }

  /**
   * This method is synchronized to make sure there will ever only be one ContextManager for this environment.
   *
   * @return
   * @throws DatabaseException
   * @throws SQLException
   */
  public ContextManager getContextManager() throws ContextManagerException {
    try {
      FinalWrapper<ContextManager> wrapper = contextManagerWrapper;
      if (wrapper == null) {
        synchronized (this) {
          wrapper = contextManagerWrapper;
          if (wrapper == null) {
            wrapper = setContextManager(new BasicContextManager(this));
          }
        }
      }
      return wrapper.value;
    } catch (Exception e) {
      throw new ContextManagerException(e.getMessage(), e);
    }
  }

  public FinalWrapper<ContextManager> setContextManager(ContextManager contextManager) {
    FinalWrapper<ContextManager> result = new FinalWrapper<ContextManager>(contextManager);
    contextManagerWrapper = result;
    return result;
  }
}
