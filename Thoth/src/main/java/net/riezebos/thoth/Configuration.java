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
package net.riezebos.thoth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.Extensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.beans.CustomRendererDefinition;
import net.riezebos.thoth.util.ConfigurationBase;

public class Configuration extends ConfigurationBase {

  private static final String DEFAULT_DATE_FMT = "dd-MM-yyyy HH:mm:ss";

  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
  private static final String WORKSPACELOCATION_DEPRECATED = "libraryroot";

  public static final String CONFIGKEY = "configuration";
  public static final String WORKSPACELOCATION = "workspacelocation";
  public static final String REQUIRED_PREFIX = "net/riezebos/thoth/skins/";
  public static final String CLASSPATH_PREFIX = "classpath:";
  public static final String SKIN_PROPERTIES = "skin.properties";
  public static final String BUILTIN_SKIN = "SimpleSkin";

  private static Configuration _instance;
  private String workspaceLocation;
  private Integer markdownOptions;

  public static Configuration getInstance() {
    if (_instance == null) {
      String propertyPath = System.getProperty(CONFIGKEY);
      if (propertyPath == null)
        propertyPath = System.getenv(CONFIGKEY);
      if (propertyPath == null) {
        String msg = "There is no configuration defined. Please set either environment or system property '" + CONFIGKEY + "' and restart";
        LOG.error(msg);
        throw new IllegalArgumentException(msg);
      } else {
        LOG.info("Using " + propertyPath + " for configuration");
        _instance = new Configuration(propertyPath);
      }
    }
    return _instance;
  }

  private Configuration(String propertyPath) {
    load(propertyPath);
  }

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

  public long getParseTimeOut() {
    return Long.parseLong(getValue("parsetimeout", "4000"));
  }

  public List<String> getBranches() {
    List<String> result = new ArrayList<>();
    for (String branch : getValue("branches", "").split("\\,"))
      if (StringUtils.isNotBlank(branch))
        result.add(branch.trim());
    Collections.sort(result);
    return result;
  }

  public void validate() {
    if (getWorkspaceLocation() == null)
      LOG.error("There is no library root defined in the configuration. " + this.getClass().getSimpleName() + "  will not be able to function");
  }

  public List<String> getBookExtensions() {
    return getValueAsSet("books", "marked,book,index");
  }

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

  public String getLocalHostUrl() {
    String url = getValue("localhost", null);
    if (!url.endsWith("/"))
      url += "/";
    return url;
  }

  public String getPdfCommand() {
    return getValue("pdf.command", null);
  }

  public long getAutoRefreshIntervalMs() {
    return Long.parseLong(getValue("versioncontrol.autorefresh", "30")) * 1000;
  }

  public boolean appendErrors() {
    return getValue("markdown.appenderrors", "true").equalsIgnoreCase("true");
  }

  public int getFileMaxRevisions() {
    return Integer.parseInt(getValue("versioncontrol.maxfilerevisions", "10"));
  }

  public int getBranchMaxRevisions() {
    return Integer.parseInt(getValue("versioncontrol.maxbranchrevisions", "10"));
  }

  public String getVersionControlType() {
    return getValue("versioncontrol.type", "git").toLowerCase();
  }

  public String getIndexExtensions() {
    return getValue("index.extensions", "md,book,marked,txt").toLowerCase();
  }

  public String getGlobalSkinBranch() {
    String branch = getValue("skin.globalbranch", null);
    if (StringUtils.isBlank(branch)) {
      String[] branches = getValue("branches", "[branches property not set]").split("\\,");
      branch = branches[0].trim();
    }
    return branch;
  }

  public String getDefaultSkin() {
    return getValue("skin.default", BUILTIN_SKIN);
  }

  public boolean isPrettyPrintJson() {
    return "true".equalsIgnoreCase(getValue("json.prettyprint", "true"));
  }

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

  public int getMaxSearchResults() {
    String value = getValue("search.maxresults", "25");
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      LOG.error("Invalid value for search.maxresults in configuration: " + value);
      return 25;
    }
  }
}
