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

import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.util.PropertyLoader;
import net.riezebos.thoth.util.ThothUtil;

public class Skin extends PropertyLoader {
  public static final String SKIN_PARENT_OF_ALL = "SimpleSkin";
  private static final String CLASSPATH_PREFIX = "classpath:";
  private String skinPropertyFile;
  private String context;
  private String contextFolder;
  private String skinBaseFolder;
  private String skinBaseUrl;
  private boolean fromClassPath = false;
  private String name;
  private String inheritsFrom;
  private Skin superSkin = null;

  /**
   * Sets up a Skin configuration
   * 
   * @param skinPropertyFile relative path to the skin.properties file in the context
   * @throws ContentManagerException
   * @throws ContextNotFoundException
   * @throws ConfigurationException 
   */
  public Skin(String context, String skinPropertyFile) throws ContextNotFoundException, ContentManagerException, ConfigurationException {
    if(StringUtils.isBlank(context)) contextFolder = "";
    else contextFolder = ContentManagerFactory.getContentManager(context).getContextFolder();
    if (skinPropertyFile.startsWith(CLASSPATH_PREFIX)) {
      fromClassPath = true;
      String resourceName = skinPropertyFile.substring(CLASSPATH_PREFIX.length());
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
      if (is == null)
        throw new ContentManagerException("Could not find " + resourceName + " on the classpath");
      load(is);
      setPropertyFileName(skinPropertyFile);
      this.skinBaseUrl = ThothUtil.getFolder(resourceName);
    } else {
      String absFileName = contextFolder + ThothUtil.stripPrefix(skinPropertyFile, "/");
      load(absFileName);
      this.skinBaseUrl = context + ThothUtil.getFolder(skinPropertyFile);
    }
    this.skinPropertyFile = skinPropertyFile;
    this.context = context;
    this.skinBaseFolder = ThothUtil.getFolder(skinPropertyFile) + "/";
    this.name = getValue("name", UUID.randomUUID().toString());
    this.inheritsFrom = getValue("inheritsfrom", null);

    if (StringUtils.isBlank(this.inheritsFrom)) {
      if (!SKIN_PARENT_OF_ALL.equalsIgnoreCase(name))
        inheritsFrom = SKIN_PARENT_OF_ALL;
    }

  }

  public String getName() {
    return name;
  }

  public String getSkinPropertyFile() {
    return skinPropertyFile;
  }

  public String getContext() {
    return context;
  }

  public String getMarkDownTemplate() {
    return getPathProperty("template.markdown");
  }

  public String getDiffTemplate() {
    return getPathProperty("template.diff");
  }

  public String getIndexTemplate() {
    return getPathProperty("template.mainindex");
  }

  public String getContextIndexTemplate() {
    return getPathProperty("template.contextindex");
  }

  public String getMetaInformationTemplate() {
    return getPathProperty("template.metainformation");
  }

  public String getRevisionTemplate() {
    return getPathProperty("template.revisions");
  }

  public String getValidationTemplate() {
    return getPathProperty("template.validation");
  }

  public String getSearchTemplate() {
    return getPathProperty("template.search");
  }

  public String getBrowseTemplate() {
    return getPathProperty("template.browse");
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
    String prefix = ThothUtil.stripPrefix(skinBaseFolder, "/");

    String result;
    // If we move into the classpath now; then do not ass the contextfolder
    if (prefix.startsWith(CLASSPATH_PREFIX))
      result = prefix + tidyRelativePath;
    else
      result = contextFolder + prefix + tidyRelativePath;
    return result;
  }

  protected boolean shouldGetFromSuper(String key) {
    return super.getValue(key, null) == null && getSuper() != null;
  }

  /**
   * Implement getting inherited values (i.e. when not set get value from super)
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
    return "Skin " + getSkinPropertyFile();
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
