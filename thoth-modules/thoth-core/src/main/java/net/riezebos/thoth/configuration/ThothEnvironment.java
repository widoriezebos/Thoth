package net.riezebos.thoth.configuration;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.impl.ClasspathContentManager;
import net.riezebos.thoth.content.impl.FSContentManager;
import net.riezebos.thoth.content.impl.GitContentManager;
import net.riezebos.thoth.content.impl.NopContentManager;
import net.riezebos.thoth.content.impl.ZipContentManager;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class ThothEnvironment implements ConfigurationChangeListener {
  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
  public static final String CONFIGKEY_DEPRECATED = "configuration";
  public static final String CONFIGKEY = "thoth_configuration";
  private static final String GLOBAL_SITE = "*global_site*";
  private static ThothEnvironment sharedThothContext = null;

  private Map<String, ContentManager> managers = new HashMap<>();
  private Configuration configuration = null;
  private List<RendererChangeListener> rendererChangeListeners = new ArrayList<>();

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
          repositoryDefinition.setType("nop");
          contentManager = registerContentManager(new NopContentManager(new ContextDefinition(repositoryDefinition, "", "", "", 0), this));
        } else {
          ContextDefinition contextDefinition = configuration.getContextDefinition(contextName);
          RepositoryDefinition repositoryDefinition = contextDefinition.getRepositoryDefinition();

          String type = repositoryDefinition.getType();
          if ("git".equalsIgnoreCase(type))
            contentManager = registerContentManager(new GitContentManager(contextDefinition, this));
          else if ("fs".equalsIgnoreCase(type) || "filesystem".equalsIgnoreCase(type))
            contentManager = registerContentManager(new FSContentManager(contextDefinition, this));
          else if ("classpath".equalsIgnoreCase(type) || "cp".equalsIgnoreCase(type))
            contentManager = registerContentManager(new ClasspathContentManager(contextDefinition, this));
          else if ("nop".equalsIgnoreCase(type))
            contentManager = registerContentManager(new NopContentManager(contextDefinition, this));
          else if ("zip".equalsIgnoreCase(type) || "jar".equalsIgnoreCase(type))
            contentManager = registerContentManager(new ZipContentManager(contextDefinition, this));
          else
            throw new ContentManagerException("Unsupported version control type: " + type);
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
    Configuration configuration = getConfiguration();
    for (String context : configuration.getContexts())
      getContentManager(context);
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
    Configuration configuration = getConfiguration();
    for (String context : configuration.getContexts())
      getContentManager(context).disableAutoRefresh();
  }

  public String pullAll() throws ContentManagerException {
    StringBuilder report = new StringBuilder();

    Configuration configuration = getConfiguration();
    for (String context : configuration.getContexts())
      report.append(getContentManager(context).refresh() + "\n");
    return report.toString();
  }

  public void reindexAll() throws ContentManagerException {
    Configuration configuration = getConfiguration();
    for (String context : configuration.getContexts())
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

  public static void registerSharedContext(ThothEnvironment thothEnvironment) {
    sharedThothContext = thothEnvironment;
  }

  public static ThothEnvironment getSharedThothContext() {
    synchronized (ThothEnvironment.class) {
      if (sharedThothContext == null)
        sharedThothContext = new ThothEnvironment();
      return sharedThothContext;
    }
  }

  public void addRendererChangedListener(RendererChangeListener listener) {
    rendererChangeListeners.add(listener);
  }

  public void removeRendererChangedListener(RendererChangeListener listener) {
    rendererChangeListeners.remove(listener);
  }
}
