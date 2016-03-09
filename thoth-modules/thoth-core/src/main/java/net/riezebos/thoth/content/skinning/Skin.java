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
package net.riezebos.thoth.content.skinning;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.util.PropertyLoader;
import net.riezebos.thoth.util.ThothUtil;

public class Skin extends PropertyLoader {
  private static final Logger LOG = LoggerFactory.getLogger(PropertyLoader.class);
  private static final String SIMPLESKIN = "net/riezebos/thoth/skins/simpleskin/skin.properties";
  private static final String CLASSPATH_PREFIX = "classpath:";
  private String context;
  private String skinBaseFolder;
  private String skinBaseUrl;
  private boolean fromClassPath = false;
  private String name;
  private String inheritsFrom;
  private Skin superSkin = null;

  public Skin() {
    try {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(SIMPLESKIN);
      load(is);
      setPropertyFileName(SIMPLESKIN);
      this.skinBaseUrl = "/";
      this.skinBaseFolder = "/";
      this.name = getValue("name", UUID.randomUUID().toString());
      this.context = "/";
    } catch (ConfigurationException e) {
      LOG.error("Problem: unable to setup even the most basic Skin. About to panic now");
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * Sets up a Skin configuration
   * 
   * @param skinPropertyFile relative path to the skin.properties file in the context
   * @throws ContentManagerException
   * @throws ContextNotFoundException
   * @throws ConfigurationException
   * @throws FileNotFoundException
   */
  public Skin(ContentManager contentManager, String skinPropertyFile) throws ContentManagerException, ConfigurationException, IOException {

    String context = contentManager.getContextName();
    setPropertyFileName(skinPropertyFile);

    if (skinPropertyFile.startsWith(CLASSPATH_PREFIX)) {
      fromClassPath = true;
      String resourceName = skinPropertyFile.substring(CLASSPATH_PREFIX.length());
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(ThothUtil.stripPrefix(resourceName, "/"));
      if (is == null)
        throw new ContentManagerException("Could not find " + resourceName + " on the classpath");
      load(is);
      this.skinBaseUrl = ThothUtil.getFolder(resourceName);
    } else {
      FileHandle fileHandle = contentManager.getFileHandle(skinPropertyFile);
      load(fileHandle.getInputStream());
      this.skinBaseUrl = context + ThothUtil.prefix(ThothUtil.getFolder(skinPropertyFile), "/");
    }
    this.skinBaseFolder = ThothUtil.suffix(ThothUtil.getFolder(skinPropertyFile), "/");
    this.name = getValue("name", UUID.randomUUID().toString());
    this.inheritsFrom = getValue("inheritsfrom", null);
    this.context = context;
  }

  public String getName() {
    return name;
  }

  public String getContext() {
    return context;
  }

  public String getHtmlTemplate() {
    return getPathProperty("template.html");
  }

  public String getDiffTemplate() {
    return getPathProperty("template.diff");
  }

  public String getIndexTemplate() {
    return getPathProperty("template.index");
  }

  public String getContextIndexTemplate() {
    return getPathProperty("template.contextindex");
  }

  public String getLoginTemplate() {
    return getPathProperty("template.login");
  }

  public String getMetaInformationTemplate() {
    return getPathProperty("template.meta");
  }

  public String getRevisionTemplate() {
    return getPathProperty("template.revisions");
  }

  public String getValidationTemplate() {
    return getPathProperty("template.validationreport");
  }

  public String getSearchTemplate() {
    return getPathProperty("template.search");
  }

  public String getBrowseTemplate() {
    return getPathProperty("template.browse");
  }

  public String getManageUsersTemplate() {
    return getPathProperty("template.manageusers");
  }

  public String getManageContextsTemplate() {
    return getPathProperty("template.managecontexts");
  }

  public String getUserProfileTemplate() {
    return getPathProperty("template.userprofile");
  }

  public String getErrorTemplate() {
    return getPathProperty("template.error");
  }

  protected String getPathProperty(String key) {
    if (shouldGetFromSuper(key))
      return getSuper().getPathProperty(key);

    String tidyRelativePath = ThothUtil.tidyRelativePath(getValue(key));
    if (isFromClassPath()) {
      return CLASSPATH_PREFIX + skinBaseUrl + "/" + tidyRelativePath;
    }
    return skinBaseFolder + tidyRelativePath;
  }

  protected boolean shouldGetFromSuper(String key) {
    return super.getValue(key, null) == null && getSuper() != null;
  }

  /**
   * Implement getting inherited values (i.e. when not set, then get value from super)
   */
  @Override
  public String getValue(String key, String dflt) {
    String ownValue = super.getValue(key, null);
    if (ownValue == null && getSuper() != null)
      ownValue = getSuper().getValue(key, dflt);
    return ownValue;
  }

  @Override
  public String toString() {
    return "Skin " + getPropertyFileName();
  }

  public String getBaseUrl() {
    return skinBaseUrl;
  }

  public String getSkinBaseFolder() {
    return skinBaseFolder;
  }

  public boolean isFromClassPath() {
    return fromClassPath;
  }

  public String getInheritsFrom() {
    return inheritsFrom;
  }

  public Skin getSuper() {
    return superSkin;
  }

  public void setSuper(Skin superSkin) {
    this.superSkin = superSkin;
  }
}
