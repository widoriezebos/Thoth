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

import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;
import net.riezebos.thoth.util.ThothUtil;

public class PropertyBasedConfiguration extends ConfigurationBase implements Configuration {

  private static final String DEFAULT_DATE_FMT = "dd-MM-yyyy HH:mm:ss";
  private static final Logger LOG = LoggerFactory.getLogger(PropertyBasedConfiguration.class);
  private static final String WORKSPACELOCATION_DEPRECATED = "libraryroot";

  private String workspaceLocation;
  private Integer markdownOptions;
  private Set<String> imageExtensions = new HashSet<>();
  private Set<String> _fragmentExtensions = null;
  private Set<String> _bookExtensions = null;

  protected PropertyBasedConfiguration(String propertyPath) throws ConfigurationException {
    loadDefaults();
    load(propertyPath);
    String extensionSpec = getImageExtensions();
    for (String ext : extensionSpec.split(extensionSpec))
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

  protected void loadDefaults() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    InputStream is = contextClassLoader.getResourceAsStream(BUILT_PROPERTIES);
    if (is == null)
      LOG.error("Somebody misplaced " + BUILT_PROPERTIES + " so there will be no defaults for the Configuration!");
    else
      load(is);
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getWorkspaceLocation()
   */
  @Override
  public String getWorkspaceLocation() {
    if (this.workspaceLocation == null) {
      String deprecated = getValue(WORKSPACELOCATION_DEPRECATED, null);
      String workspaceLocation = getValue(WORKSPACELOCATION, deprecated);
      if (workspaceLocation != null) {
        workspaceLocation = workspaceLocation.replaceAll("\\\\", "/");
        if (!workspaceLocation.endsWith("/"))
          workspaceLocation += "/";
        this.workspaceLocation = workspaceLocation;
      }
    }
    return this.workspaceLocation;
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
    if (this.markdownOptions == null) {
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
      this.markdownOptions = result;
    }
    return this.markdownOptions;
  }

  private boolean isOptionOn(String key) {
    String value = getValue("markdown.option." + key, "off");
    return value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1");
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
    return getValue("markdown.appenderrors", "true").equalsIgnoreCase("true");
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
  public String getGlobalSkinContext() {
    String context = getValue("skin.globalcontext", null);
    if (StringUtils.isBlank(context)) {
      List<String> contexts = getContexts();
      if (!contexts.isEmpty())
        context = contexts.get(0);
    }
    return context;
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getDefaultSkin()
   */
  @Override
  public String getDefaultSkin() {
    return getValue("skin.default", Skin.SKIN_PARENT_OF_ALL);
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#getContextIndexClassifications()
   */
  @Override
  public Set<String> getContextIndexClassifications() {
    String value = getValue("context.classifications", "category,audience,folder");
    Set<String> classifications = new HashSet<>();
    for (String name : value.split("\\,")) {
      classifications.add(name.trim());
    }
    return classifications;
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#isPrettyPrintJson()
   */
  @Override
  public boolean isPrettyPrintJson() {
    return "true".equalsIgnoreCase(getValue("json.prettyprint", "true"));
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
      if (extension != null) {
        renderer = new CustomRendererDefinition(extension, contenttype, command);
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
  public SimpleDateFormat getDateFormat() {
    try {
      return new SimpleDateFormat(getDateFormatMask());
    } catch (Exception x) {
      LOG.warn("Invalid format mask: " + getDateFormatMask());
      return new SimpleDateFormat(DEFAULT_DATE_FMT);
    }
  }

  public String getDateFormatMask() {
    return getValue("formatmask", DEFAULT_DATE_FMT);
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
    if (_fragmentExtensions == null) {
      Set<String> fragmentExtensions = new HashSet<>();
      fragmentExtensions.addAll(getDocumentExtensions());
      fragmentExtensions.removeAll(getBookExtensions());
      _fragmentExtensions = fragmentExtensions;
    }
    if (ThothUtil.getFileName(path).startsWith("."))
      return false;

    String extension = ThothUtil.getExtension(path);
    if (extension == null)
      return false;
    else
      extension = extension.toLowerCase();
    return _fragmentExtensions.contains(extension);
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#isBook(java.lang.String)
   */
  @Override
  public boolean isBook(String path) {
    if (_bookExtensions == null) {
      Set<String> bookExtensions = new HashSet<>();
      bookExtensions.removeAll(getBookExtensions());
      _bookExtensions = bookExtensions;
    }
    if (ThothUtil.getFileName(path).startsWith("."))
      return false;

    String extension = ThothUtil.getExtension(path);
    if (extension == null)
      return false;
    else
      extension = extension.toLowerCase();
    return _bookExtensions.contains(extension);
  }

  /*
   * (non-Javadoc)
   * @see net.riezebos.thoth.configuration.ConfigurationT#isResource(java.lang.String)
   */
  @Override
  public boolean isResource(String path) {
    return !isFragment(path) && !isBook(path);
  }
}
