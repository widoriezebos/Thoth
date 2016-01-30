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
package net.riezebos.thoth.content;

import java.io.InputStream;

import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.ConfigurationBase;
import net.riezebos.thoth.util.ThothUtil;

public class Skin extends ConfigurationBase {
  private static final String CLASSPATH_PREFIX = "classpath:";
  private String skinPropertyFile;
  private String branch;
  private String skinBaseFolder;
  private String skinBaseUrl;
  private boolean fromClassPath = false;

  /**
   * Sets up a Skin configuration
   * 
   * @param skinPropertyFile relative path to the skin.properties file in the branch
   * @throws ContentManagerException
   * @throws BranchNotFoundException
   */
  public Skin(String branch, String skinPropertyFile) throws BranchNotFoundException, ContentManagerException {
    if (skinPropertyFile.startsWith(CLASSPATH_PREFIX)) {
      fromClassPath = true;
      String resourceName = skinPropertyFile.substring(CLASSPATH_PREFIX.length());
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
      if (is == null)
        throw new ContentManagerException("Could not find " + resourceName + " on the classpath");
      load(is);
      this.skinBaseUrl = ThothUtil.getFolder(resourceName);
    } else {
      String absFileName = ContentManagerFactory.getContentManager().getBranchFolder(branch) + skinPropertyFile;
      load(absFileName);
      this.skinBaseUrl = branch + "/" + ThothUtil.getFolder(skinPropertyFile);
    }
    this.skinPropertyFile = skinPropertyFile;
    this.branch = branch;
    this.skinBaseFolder = ThothUtil.getFolder(getPropertyFileName()) + "/";
  }

  public String getSkinPropertyFile() {
    return skinPropertyFile;
  }

  public String getBranch() {
    return branch;
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

  public String getBranchIndexTemplate() {
    return getPathProperty("template.branchindex");
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

  protected String getPathProperty(String key) {
    String tidyRelativePath = ThothUtil.tidyRelativePath(getValue(key));
    if (fromClassPath) {
      if (tidyRelativePath.startsWith(CLASSPATH_PREFIX)) {
        tidyRelativePath = tidyRelativePath.substring(CLASSPATH_PREFIX.length());
        return CLASSPATH_PREFIX + skinBaseUrl + "/" + tidyRelativePath;
      }
    }
    return skinBaseFolder + tidyRelativePath;
  }

  @Override
  public String toString() {
    return "Skin :" + getSkinPropertyFile();
  }

  public String getBaseUrl() {
    return skinBaseUrl;
  }

  public boolean isFromClassPath() {
    return fromClassPath;
  }
}
