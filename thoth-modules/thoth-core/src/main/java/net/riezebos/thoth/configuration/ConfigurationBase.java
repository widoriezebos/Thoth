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

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.Extensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.context.RepositoryType;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.renderers.HtmlRenderer;
import net.riezebos.thoth.renderers.RawRenderer;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;
import net.riezebos.thoth.util.ThothUtil;

public abstract class ConfigurationBase implements Configuration {

  private static final String SKINSUBSTITUTION_PREFIX = "skinsubstitution";
  private static final String DEFAULT_DATE_FMT = "dd-MM-yyyy";
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationBase.class);
  private static final String WORKSPACELOCATION_DEPRECATED = "libraryroot";
  private Map<String, RepositoryDefinition> repositoryDefinitions = new HashMap<>();
  private Map<String, ContextDefinition> contextDefinitions = new HashMap<>();
  private String workspaceLocation;
  private Integer markdownOptions;
  protected Set<String> imageExtensions = new HashSet<>();
  private Set<String> fragmentExtensions = null;
  private Set<String> bookExtensions = null;
  private Map<String, String> skinSubstitutions = null;
  private List<String> classifications = null;
  private List<String> outputFormats = null;
  private String sourceSpec;

  /**
   * Returns an entire branch (folder) as key/value pairs
   */
  protected abstract Map<String, String> mapBranch(String path);

  /**
   * Return a value for a key. Empty/whitespace values will be returned as null
   */
  public abstract String getValue(String key);

  public ConfigurationBase() {
  }

  @Override
  public Configuration clone() {
    try {
      ConfigurationBase clone = (ConfigurationBase) super.clone();
      clone.repositoryDefinitions = new HashMap<>();
      clone.contextDefinitions = new HashMap<>();
      repositoryDefinitions.entrySet().stream().forEach(entry -> clone.repositoryDefinitions.put(entry.getKey(), entry.getValue().clone()));
      contextDefinitions.entrySet().stream().forEach(entry -> clone.contextDefinitions.put(entry.getKey(), entry.getValue().clone()));
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalArgumentException();
    }
  }

  protected void clear() {
    workspaceLocation = null;
    markdownOptions = null;
    imageExtensions = new HashSet<>();
    fragmentExtensions = null;
    bookExtensions = null;
    skinSubstitutions = null;
    classifications = null;
    outputFormats = null;
    repositoryDefinitions = new HashMap<>();
    contextDefinitions = new HashMap<>();
  }

  @Override
  public String getValue(String key, String dflt) {
    String result = getValue(key);
    return result == null ? dflt : result;
  }

  @Override
  public String getSourceSpec() {
    return sourceSpec;
  }

  public void setSourceSpec(String sourceSpec) {
    this.sourceSpec = sourceSpec;
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

  @Override
  public String getImageExtensions() {
    return getValue("images.extensions", "png,jpeg,jpg,gif,tiff,bmp");
  }

  protected Properties getDefaults() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    Properties result = new Properties();
    InputStream is = contextClassLoader.getResourceAsStream(BUILTIN_PROPERTIES);
    if (is == null) {
      LOG.error("Somebody misplaced " + BUILTIN_PROPERTIES + " so there will be no defaults for the Configuration!");
    } else {
      try {
        result.load(is);
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
    return result;
  }

  @Override
  public String getWorkspaceLocation() {
    if (workspaceLocation == null) {
      String deprecated = getValue(WORKSPACELOCATION_DEPRECATED, null);
      String workspaceLocation = getValue(WORKSPACELOCATION, deprecated);
      if (workspaceLocation != null) {
        workspaceLocation = workspaceLocation.replaceAll("\\\\", "/");
        this.workspaceLocation = ThothUtil.suffix(workspaceLocation, "/");
      }
    }
    return workspaceLocation;
  }

  @Override
  public long getParseTimeOut() {
    return Long.parseLong(getValue("parsetimeout", "4000"));
  }

  @Override
  public void validate() throws ConfigurationException {
    if (getWorkspaceLocation() == null)
      LOG.error("There is no library root defined in the configuration. " + this.getClass().getSimpleName() + "  will not be able to function");
  }

  @Override
  public List<String> getBookExtensions() {
    return getValueAsSet("books", "marked,book,index");
  }

  @Override
  public List<String> getDocumentExtensions() {
    return getValueAsSet("documents", "marked,book,index,md");
  }

  protected List<String> getValueAsSet(String key, String defaultValue) {
    List<String> result = new ArrayList<>();
    for (String extension : getValue(key, defaultValue).split("\\,"))
      if (StringUtils.isNotBlank(extension))
        result.add(extension.trim());
    Collections.sort(result);
    return result;
  }

  @Override
  public int getMarkdownOptions() {
    if (markdownOptions == null) {
      int result = 0;
      if (isOptionOn("SMARTS"))
        result |= Extensions.SMARTS;
      if (isOptionOn("QUOTES"))
        result |= Extensions.QUOTES;
      if (isOptionOn("ABBREVIATIONS"))
        result |= Extensions.ABBREVIATIONS;
      if (isOptionOn("HARDWRAPS"))
        result |= Extensions.HARDWRAPS;
      if (isOptionOn("AUTOLINKS"))
        result |= Extensions.AUTOLINKS;
      if (isOptionOn("TABLES"))
        result |= Extensions.TABLES;
      if (isOptionOn("DEFINITIONS"))
        result |= Extensions.DEFINITIONS;
      if (isOptionOn("FENCED_CODE_BLOCKS"))
        result |= Extensions.FENCED_CODE_BLOCKS;
      if (isOptionOn("WIKILINKS"))
        result |= Extensions.WIKILINKS;
      if (isOptionOn("STRIKETHROUGH"))
        result |= Extensions.STRIKETHROUGH;
      if (isOptionOn("ANCHORLINKS"))
        result |= Extensions.ANCHORLINKS;
      if (isOptionOn("SUPPRESS_HTML_BLOCKS"))
        result |= Extensions.SUPPRESS_HTML_BLOCKS;
      if (isOptionOn("SUPPRESS_INLINE_HTML"))
        result |= Extensions.SUPPRESS_INLINE_HTML;
      if (isOptionOn("ATXHEADERSPACE"))
        result |= Extensions.ATXHEADERSPACE;
      if (isOptionOn("FORCELISTITEMPARA"))
        result |= Extensions.FORCELISTITEMPARA;
      if (isOptionOn("RELAXEDHRULES"))
        result |= Extensions.RELAXEDHRULES;
      if (isOptionOn("TASKLISTITEMS"))
        result |= Extensions.TASKLISTITEMS;
      if (isOptionOn("EXTANCHORLINKS"))
        result |= Extensions.EXTANCHORLINKS;
      markdownOptions = result;
    }
    return markdownOptions;
  }

  private boolean isOptionOn(String key) {
    return isOn(getValue("markdown.option." + key, "off"));
  }

  @Override
  public String getLocalHostUrl() {
    String url = getValue("localhost", null);
    if (!url.endsWith("/"))
      url += "/";
    return url;
  }

  @Override
  public boolean appendErrors() {
    return isOn(getValue("markdown.appenderrors", "true"));
  }

  @Override
  public boolean addNewlineBeforeheader() {
    return isOn(getValue("markdown.newlineheaders", "true"));
  }

  protected boolean isOn(String value) {
    return value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
  }

  @Override
  public int getFileMaxRevisions() {
    return Integer.parseInt(getValue("versioncontrol.maxfilerevisions", "10"));
  }

  @Override
  public int getEmbeddedServerPort() {
    return Integer.parseInt(getValue("embedded.port", "8080"));
  }

  @Override
  public String getEmbeddedServerName() {
    return getValue("embedded.servername", "localhost");
  }

  @Override
  public int getEmbeddedIdleTimeout() {
    return Integer.parseInt(getValue("embedded.idletimeout", "30"));
  }

  @Override
  public int getContextMaxRevisions() {
    return Integer.parseInt(getValue("versioncontrol.maxcontextrevisions", "10"));
  }

  @Override
  public String getIndexExtensions() {
    return getValue("index.extensions", "md,book,marked,txt").toLowerCase();
  }

  @Override
  public String getMainIndexSkinContext() {
    return getValue("skin.mainindexcontext", null);
  }

  @Override
  public String getDefaultSkin() {
    return getValue("skin.default", SkinManager.SKIN_PARENT_OF_ALL);
  }

  @Override
  public List<String> getContextIndexClassifications() {
    if (classifications == null) {
      String value = getValue("context.classifications", "category,audience,folder");
      Set<String> classificationSet = new HashSet<>();
      for (String name : value.split("\\,")) {
        classificationSet.add(name.trim());
      }
      List<String> lst = new ArrayList<>(classificationSet);
      Collections.sort(lst);
      classifications = lst;
    }
    return classifications;
  }

  @Override
  public List<String> getOutputFormats() {
    if (outputFormats == null) {
      List<String> lst = new ArrayList<>();
      lst.add(HtmlRenderer.TYPE);
      lst.add(RawRenderer.TYPE);
      for (CustomRendererDefinition customRenderer : getCustomRenderers())
        lst.add(customRenderer.getExtension());
      outputFormats = lst;
    }
    return outputFormats;
  }

  @Override
  public boolean isPrettyPrintJson() {
    return isOn(getValue("json.prettyprint", "true"));
  }

  @Override
  public List<CustomRendererDefinition> getCustomRenderers() {
    List<CustomRendererDefinition> result = new ArrayList<>();

    CustomRendererDefinition renderer = null;
    int idx = 0;
    do {
      renderer = null;
      idx++;
      String extension = getValue("renderer." + idx + ".extension", null);
      String contenttype = getValue("renderer." + idx + ".contenttype", null);
      String command = getValue("renderer." + idx + ".command", null);
      String source = getValue("renderer." + idx + ".source", null);
      if (StringUtils.isBlank(source))
        source = "html";
      if (extension != null) {
        renderer = new CustomRendererDefinition(extension, contenttype, source, command);
        result.add(renderer);
      }
    } while (renderer != null);

    return result;
  }

  @Override
  public SimpleDateFormat getTimestampFormat() {
    try {
      return new SimpleDateFormat(getTimestampFormatMask());
    } catch (Exception x) {
      LOG.warn("Invalid format mask: " + getTimestampFormatMask());
      return new SimpleDateFormat(DEFAULT_TIMESTAMP_FMT);
    }
  }

  @Override
  public SimpleDateFormat getDateFormat() {
    try {
      return new SimpleDateFormat(getDateFormatMask());
    } catch (Exception x) {
      LOG.warn("Invalid format mask: " + getTimestampFormatMask());
      return new SimpleDateFormat(DEFAULT_DATE_FMT);
    }
  }

  public String getTimestampFormatMask() {
    return getValue("formatmask.timestamp", DEFAULT_TIMESTAMP_FMT);
  }

  public String getDateFormatMask() {
    return getValue("formatmask.date", DEFAULT_DATE_FMT);
  }

  @Override
  public int getMaxSearchResults() {
    String value = getValue("search.maxresults", "25");
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      LOG.error("Invalid value for search.maxresults in configuration: " + value);
      return 25;
    }
  }

  @Override
  public boolean isImageExtension(String extension) {
    if (extension == null)
      return false;
    return imageExtensions.contains(extension.toLowerCase());
  }

  @Override
  public boolean isFragment(String path) {
    if (fragmentExtensions == null) {
      Set<String> fragmentExtensions = new HashSet<>();
      fragmentExtensions.addAll(getDocumentExtensions());
      fragmentExtensions.removeAll(getBookExtensions());
      this.fragmentExtensions = fragmentExtensions;
    }
    if (ThothUtil.getFileName(path).startsWith("."))
      return false;

    String extension = ThothUtil.getExtension(path);
    if (extension == null)
      return false;
    else
      extension = extension.toLowerCase();
    return fragmentExtensions.contains(extension);
  }

  @Override
  public boolean isBook(String path) {
    if (bookExtensions == null) {
      bookExtensions = new HashSet<>(getBookExtensions());
    }
    if (ThothUtil.getFileName(path).startsWith("."))
      return false;

    String extension = ThothUtil.getExtension(path);
    if (extension == null)
      return false;
    else
      extension = extension.toLowerCase();
    return bookExtensions.contains(extension);
  }

  @Override
  public boolean isResource(String path) {
    return !isFragment(path) && !isBook(path);
  }

  @Override
  public int getMaxHeaderNumberingLevel() {
    return Integer.parseInt(getValue("markdown.maxheadernumberlevel", "3"));
  }

  @Override
  public boolean isAutoReload() {
    return isOn(getValue("config.autorefresh", "true"));
  }

  @Override
  public int getAutoReloadInterval() {
    return Integer.parseInt(getValue("config.autorefreshinterval", "30"));
  }

  @Override
  public String getDefaultGroup() {
    return getValue("defaultgroup", "readers");
  }

  @Override
  public String getDatabaseType() {
    return getValue("database.type", "embedded");
  }

  @Override
  public String getDatabaseUrl() {
    return getValue("database.url", getWorkspaceLocation() + "thoth-database");
  }

  @Override
  public String getDatabaseUser() {
    return getValue("database.user", "thoth");
  }

  @Override
  public String getDatabasePassword() {
    return getValue("database.password", "thoth");
  }

  @Override
  public String getPasswordEncryptionKey() {
    return getValue("masterpassword", "InFactYouShouldChangeThisRightAfterSetupAndNot1SecondL8er!");
  }

  @Override
  public String getServerName() {
    return getValue("servername", "Thoth");
  }

  @Override
  public String getSkinSubstitution(String original) {
    return getSkinSubstitutions().get(original);
  }

  protected Map<String, String> getSkinSubstitutions() {
    if (skinSubstitutions == null) {
      Map<String, String> subst = mapBranch(SKINSUBSTITUTION_PREFIX);
      skinSubstitutions = subst;
    }
    return skinSubstitutions;
  }
}
