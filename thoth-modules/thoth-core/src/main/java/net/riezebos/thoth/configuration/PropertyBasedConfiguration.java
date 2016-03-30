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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.Extensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.renderers.HtmlRenderer;
import net.riezebos.thoth.renderers.RawRenderer;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;
import net.riezebos.thoth.util.ThothUtil;

public class PropertyBasedConfiguration extends ConfigurationBase implements Configuration {

  private static final String CLASSPATH_PREFIX = "classpath:";

  private static final String DEFAULT_DATE_FMT = "dd-MM-yyyy";

  private static final Logger LOG = LoggerFactory.getLogger(PropertyBasedConfiguration.class);
  private static final String WORKSPACELOCATION_DEPRECATED = "libraryroot";

  private String workspaceLocation;
  private Integer markdownOptions;
  private Set<String> imageExtensions = new HashSet<>();
  private Set<String> fragmentExtensions = null;
  private Set<String> bookExtensions = null;
  private List<String> classifications = null;
  private List<String> outputFormats = null;

  public PropertyBasedConfiguration() throws ConfigurationException {
  }

  @Override
  protected void clear() {
    super.clear();
    workspaceLocation = null;
    markdownOptions = null;
    imageExtensions = new HashSet<>();
    fragmentExtensions = null;
    bookExtensions = null;
    classifications = null;
    outputFormats = null;
  }

  @Override
  public void load(InputStream inStream) throws ConfigurationException {
    super.load(inStream);
    String extensionSpec = getImageExtensions();
    for (String ext : extensionSpec.split("\\,"))
      imageExtensions.add(ext.trim().toLowerCase());
    loadRepositoryDefinitions();
    loadContextDefinitions();
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getImageExtensions()
   */
  @Override
  public String getImageExtensions() {
    return getValue("images.extensions", "png,jpeg,jpg,gif,tiff,bmp");
  }

  @Override
  protected void loadDefaults() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    InputStream is = contextClassLoader.getResourceAsStream(BUILT_PROPERTIES);
    if (is == null)
      LOG.error("Somebody misplaced " + BUILT_PROPERTIES + " so there will be no defaults for the Configuration!");
    else
      readInputStream(is);
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getWorkspaceLocation()
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getParseTimeOut()
   */
  @Override
  public long getParseTimeOut() {
    return Long.parseLong(getValue("parsetimeout", "4000"));
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#validate()
   */
  @Override
  public void validate() throws ConfigurationException {
    if (getWorkspaceLocation() == null)
      LOG.error("There is no library root defined in the configuration. " + this.getClass().getSimpleName() + "  will not be able to function");
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getBookExtensions()
   */
  @Override
  public List<String> getBookExtensions() {
    return getValueAsSet("books", "marked,book,index");
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getDocumentExtensions()
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getMarkdownOptions()
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getLocalHostUrl()
   */
  @Override
  public String getLocalHostUrl() {
    String url = getValue("localhost", null);
    if (!url.endsWith("/"))
      url += "/";
    return url;
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#appendErrors()
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getFileMaxRevisions()
   */
  @Override
  public int getFileMaxRevisions() {
    return Integer.parseInt(getValue("versioncontrol.maxfilerevisions", "10"));
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getEmbeddedServerPort()
   */
  @Override
  public int getEmbeddedServerPort() {
    return Integer.parseInt(getValue("embedded.port", "8080"));
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getEmbeddedServerName()
   */
  @Override
  public String getEmbeddedServerName() {
    return getValue("embedded.servername", "localhost");
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getEmbeddedIdleTimeout()
   */
  @Override
  public int getEmbeddedIdleTimeout() {
    return Integer.parseInt(getValue("embedded.idletimeout", "30"));
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getContextMaxRevisions()
   */
  @Override
  public int getContextMaxRevisions() {
    return Integer.parseInt(getValue("versioncontrol.maxcontextrevisions", "10"));
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getIndexExtensions()
   */
  @Override
  public String getIndexExtensions() {
    return getValue("index.extensions", "md,book,marked,txt").toLowerCase();
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getGlobalSkinContext()
   */
  @Override
  public String getMainIndexSkinContext() {
    return getValue("skin.mainindexcontext", null);
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getDefaultSkin()
   */
  @Override
  public String getDefaultSkin() {
    return getValue("skin.default", SkinManager.SKIN_PARENT_OF_ALL);
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getContextIndexClassifications()
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getContextIndexClassifications()
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#isPrettyPrintJson()
   */
  @Override
  public boolean isPrettyPrintJson() {
    return isOn(getValue("json.prettyprint", "true"));
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getCustomRenderers()
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getDateFormat()
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getMaxSearchResults()
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#isImageExtension(java.lang.String)
   */
  @Override
  public boolean isImageExtension(String extension) {
    if (extension == null)
      return false;
    return imageExtensions.contains(extension.toLowerCase());
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#isFragment(java.lang.String)
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#isBook(java.lang.String)
   */
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

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#isResource(java.lang.String)
   */
  @Override
  public boolean isResource(String path) {
    return !isFragment(path) && !isBook(path);
  }

  @Override
  public int getMaxHeaderNumberingLevel() {
    return Integer.parseInt(getValue("markdown.maxheadernumberlevel", "3"));
  }

  @Override
  public void reload() throws FileNotFoundException, ConfigurationException {
    clear();
    String propertyFileName = getPropertyFileName();
    InputStream inStream;
    if (propertyFileName.startsWith(CLASSPATH_PREFIX))
      inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFileName.substring(CLASSPATH_PREFIX.length()));
    else
      inStream = new FileInputStream(propertyFileName);

    if (inStream == null)
      throw new FileNotFoundException(propertyFileName);
    load(inStream);
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
}
