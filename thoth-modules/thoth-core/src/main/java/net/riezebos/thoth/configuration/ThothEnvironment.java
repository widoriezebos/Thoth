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
  private static FinalWrapper<ThothEnvironment> sharedThothContextWrapper = null;

  private Map<String, ContentManager> managers = new HashMap<>();
  private Configuration configuration = null;
  private List<RendererChangeListener> rendererChangeListeners = new ArrayList<>();
  private FinalWrapper<ThothDB> thothDbWrapper = null;
  private FinalWrapper<UserManager> userManagerWrapper = null;
  private FinalWrapper<ContextManager> contextManagerWrapper = null;
  private FinalWrapper<ExpiringCache<String, Integer>> expiringCacheWrapper = null;

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
    for (String context : getContextManager().getContexts())
      try {
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
    for (String context : getContextManager().getContexts())
      getContentManager(context).disableAutoRefresh();
  }

  public String pullAll() throws ContentManagerException {
    StringBuilder report = new StringBuilder();

    for (String context : getContextManager().getContexts())
      report.append(getContentManager(context).refresh() + "\n");
    return report.toString();
  }

  public void reindexAll() throws ContentManagerException {
    for (String context : getContextManager().getContexts())
      getContentManager(context).reindex();
  }

  public Configuration getConfiguration() {
    try {
      if (configuration == null) {
        String propertyPath = determinePropertyPath();
        if (propertyPath == null) {
          String msg = "There is no configuration defined. Please set either environment or system property '" + CONFIGKEY + "' and restart";
          LOG.error(msg);
          throw new IllegalArgumentException(msg);
        } else {
          LOG.info("Using " + propertyPath + " for configuration");
          PropertyBasedConfiguration propertyBasedConfiguration = new PropertyBasedConfiguration();
          propertyBasedConfiguration.setPropertyFileName(propertyPath);
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
   * This method is synchronized to make sure there will ever only be one shared ThothEnvironment
   * 
   * @return
   */
  public static ThothEnvironment getSharedThothContext() {
    FinalWrapper<ThothEnvironment> wrapper = sharedThothContextWrapper;
    if (wrapper == null) {
      synchronized (ThothEnvironment.class) {
        if (sharedThothContextWrapper == null) {
          sharedThothContextWrapper = new FinalWrapper<ThothEnvironment>(new ThothEnvironment());
        }
        wrapper = sharedThothContextWrapper;
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
        if (thothDbWrapper == null) {
          thothDbWrapper = new FinalWrapper<ThothDB>(createThothDB());
        }
        wrapper = thothDbWrapper;
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
  public ExpiringCache<String, Integer> getLoginFailCounters() throws DatabaseException {
    FinalWrapper<ExpiringCache<String, Integer>> wrapper = expiringCacheWrapper;
    if (wrapper == null) {
      synchronized (this) {
        if (expiringCacheWrapper == null) {
          expiringCacheWrapper = new FinalWrapper<ExpiringCache<String, Integer>>(new ExpiringCache<>(FIVE_MINUTES));
        }
        wrapper = expiringCacheWrapper;
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
        if (userManagerWrapper == null) {
          setUserManager(new BasicUserManager(this));
        }
        wrapper = userManagerWrapper;
      }
    }
    return wrapper.value;
  }

  public void setUserManager(UserManager userManager) {
    userManagerWrapper = new FinalWrapper<UserManager>(userManager);
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
          if (contextManagerWrapper == null) {
            setContextManager(new BasicContextManager(this));
          }
          wrapper = contextManagerWrapper;
        }
      }
      return wrapper.value;
    } catch (Exception e) {
      throw new ContextManagerException(e.getMessage(), e);
    }
  }

  public void setContextManager(ContextManager contextManager) {
    contextManagerWrapper = new FinalWrapper<ContextManager>(contextManager);
  }

}
