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

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;

/**
 * Supports hot-reloading of the configuration (in a thread safe manner). Also makes sure that when a reload fails that the active configuration remains valid.
 * 
 * @author wido
 */
public class HotReloadableConfiguration implements Configuration {
  private static final Logger LOG = LoggerFactory.getLogger(HotReloadableConfiguration.class);

  private Configuration activeConfiguration;
  private List<ConfigurationChangeListener> listeners = new ArrayList<>();
  private boolean autoRefresh = false;
  private long configFileModified;

  public HotReloadableConfiguration(Configuration configuration) {
    activeConfiguration = configuration;
    configFileModified = getModificationTime(configuration);
    configureAutoReload();
  }

  protected long getModificationTime(Configuration configuration) {
    if (configuration.getPropertyFileName() == null)
      return 0L;
    return new File(configuration.getPropertyFileName()).lastModified();
  }

  @Override
  public void discard() {
    activeConfiguration.discard();
  }

  public void addConfigurationChangeListener(ConfigurationChangeListener listener) {
    listeners.add(listener);
  }

  public void removeConfigurationChangeListener(ConfigurationChangeListener listener) {
    listeners.remove(listener);
  }

  /**
   * Reload the configuration using a temporary clone. Only if and when the reload succeeds (and is complete) will the active configuration be replaced
   * atomically. Also notify any listeners of changes to contexts.
   */
  synchronized public void reload() throws FileNotFoundException, ConfigurationException {
    Set<ContextDefinition> originalContextDefinitions = new HashSet<>(activeConfiguration.getContextDefinitions().values());
    List<CustomRendererDefinition> originalCustomRenderers = activeConfiguration.getCustomRenderers();

    Configuration newOne = activeConfiguration.clone();
    boolean reloadWasOn = activeConfiguration.isAutoReload();
    newOne.reload();
    activeConfiguration = newOne;

    List<CustomRendererDefinition> newCustomRenderers = activeConfiguration.getCustomRenderers();
    if (!originalCustomRenderers.equals(newCustomRenderers))
      notifyRendersChanges();

    Set<ContextDefinition> newContextDefinitions = new HashSet<>(activeConfiguration.getContextDefinitions().values());

    for (ContextDefinition original : originalContextDefinitions) {
      if (!newContextDefinitions.contains(original))
        notifyContextRemoved(original);
    }
    for (ContextDefinition newCtxt : newContextDefinitions) {
      if (!originalContextDefinitions.contains(newCtxt))
        notifyContextAdded(newCtxt);
    }

    if (activeConfiguration.isAutoReload() && !reloadWasOn)
      configureAutoReload();
    else
      autoRefresh = activeConfiguration.isAutoReload();
    
    configFileModified = getModificationTime(activeConfiguration);
  }

  private void notifyRendersChanges() {
    LOG.info("Renderers changed in the configuration. Notifying listeners");
    for (ConfigurationChangeListener listener : listeners)
      listener.renderersChanged();
  }

  protected void notifyContextAdded(ContextDefinition context) {
    LOG.info("Contexts added to the configuration. Notifying listeners");
    for (ConfigurationChangeListener listener : listeners)
      listener.contextAdded(context);
  }

  protected void notifyContextRemoved(ContextDefinition context) {
    LOG.info("Contexts removed from the configuration. Notifying listeners");
    for (ConfigurationChangeListener listener : listeners)
      listener.contextRemoved(context);
  }

  protected void configureAutoReload() {
    if (activeConfiguration.isAutoReload()) {

      final int autoReloadInterval = activeConfiguration.getAutoReloadInterval() * 1000;
      new Thread() {
        @Override
        public void run() {
          do {
            try {
              Thread.sleep(autoReloadInterval);
            } catch (InterruptedException e) {
            }
            checkForChanges();
          } while (autoRefresh);
        }
      }.start();
    }
  }

  protected void checkForChanges() {
    long checkModified = getModificationTime(activeConfiguration);

    if (checkModified != configFileModified) {
      try {
        reload();
        LOG.info("Configuration changes detected, reconfiguration occurred");
      } catch (FileNotFoundException | ConfigurationException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  @Override
  public HotReloadableConfiguration clone() {
    HotReloadableConfiguration clone;
    try {
      clone = (HotReloadableConfiguration) super.clone();
      clone.activeConfiguration = activeConfiguration.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public String getImageExtensions() {
    return activeConfiguration.getImageExtensions();
  }

  public String getWorkspaceLocation() {
    return activeConfiguration.getWorkspaceLocation();
  }

  public long getParseTimeOut() {
    return activeConfiguration.getParseTimeOut();
  }

  public List<String> getContexts() {
    return activeConfiguration.getContexts();
  }

  public void validate() throws ConfigurationException {
    activeConfiguration.validate();
  }

  public List<String> getBookExtensions() {
    return activeConfiguration.getBookExtensions();
  }

  public List<String> getDocumentExtensions() {
    return activeConfiguration.getDocumentExtensions();
  }

  public int getMarkdownOptions() {
    return activeConfiguration.getMarkdownOptions();
  }

  public String getLocalHostUrl() {
    return activeConfiguration.getLocalHostUrl();
  }

  public int getMaxHeaderNumberingLevel() {
    return activeConfiguration.getMaxHeaderNumberingLevel();
  }

  public boolean appendErrors() {
    return activeConfiguration.appendErrors();
  }

  public int getFileMaxRevisions() {
    return activeConfiguration.getFileMaxRevisions();
  }

  public int getEmbeddedServerPort() {
    return activeConfiguration.getEmbeddedServerPort();
  }

  public String getEmbeddedServerName() {
    return activeConfiguration.getEmbeddedServerName();
  }

  public int getEmbeddedIdleTimeout() {
    return activeConfiguration.getEmbeddedIdleTimeout();
  }

  public int getContextMaxRevisions() {
    return activeConfiguration.getContextMaxRevisions();
  }

  public String getIndexExtensions() {
    return activeConfiguration.getIndexExtensions();
  }

  public String getMainIndexSkinContext() {
    return activeConfiguration.getMainIndexSkinContext();
  }

  public String getDefaultSkin() {
    return activeConfiguration.getDefaultSkin();
  }

  public List<String> getContextIndexClassifications() {
    return activeConfiguration.getContextIndexClassifications();
  }

  public boolean isPrettyPrintJson() {
    return activeConfiguration.isPrettyPrintJson();
  }

  public List<CustomRendererDefinition> getCustomRenderers() {
    return activeConfiguration.getCustomRenderers();
  }

  public SimpleDateFormat getTimestampFormat() {
    return activeConfiguration.getTimestampFormat();
  }

  public SimpleDateFormat getDateFormat() {
    return activeConfiguration.getDateFormat();
  }

  public int getMaxSearchResults() {
    return activeConfiguration.getMaxSearchResults();
  }

  public boolean isImageExtension(String extension) {
    return activeConfiguration.isImageExtension(extension);
  }

  public boolean isFragment(String path) {
    return activeConfiguration.isFragment(path);
  }

  public boolean isBook(String path) {
    return activeConfiguration.isBook(path);
  }

  public boolean isResource(String path) {
    return activeConfiguration.isResource(path);
  }

  public String getValue(String key) {
    return activeConfiguration.getValue(key);
  }

  public String getValue(String key, String dflt) {
    return activeConfiguration.getValue(key, dflt);
  }

  public Map<String, ContextDefinition> getContextDefinitions() {
    return activeConfiguration.getContextDefinitions();
  }

  public Map<String, RepositoryDefinition> getRepositoryDefinitions() {
    return activeConfiguration.getRepositoryDefinitions();
  }

  public ContextDefinition getContextDefinition(String name) throws ContextNotFoundException {
    return activeConfiguration.getContextDefinition(name);
  }

  public boolean isValidContext(String name) {
    return activeConfiguration.isValidContext(name);
  }

  public List<String> getOutputFormats() {
    return activeConfiguration.getOutputFormats();
  }

  public String getPropertyFileName() {
    return activeConfiguration.getPropertyFileName();
  }

  @Override
  public boolean addNewlineBeforeheader() {
    return activeConfiguration.addNewlineBeforeheader();
  }

  @Override
  public boolean isAutoReload() {
    return activeConfiguration.isAutoReload();
  }

  @Override
  public int getAutoReloadInterval() {
    return activeConfiguration.getAutoReloadInterval();
  }

}