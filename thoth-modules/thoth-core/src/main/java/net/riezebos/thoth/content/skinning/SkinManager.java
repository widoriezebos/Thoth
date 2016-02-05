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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.CacheManager;
import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.ThothUtil;

public class SkinManager {

  private static final Logger LOG = LoggerFactory.getLogger(SkinManager.class);
  private static final String BUILTIN_SKIN_LIST = "net/riezebos/thoth/skins/builtinskins.txt";
  private static final String SKINS_PROPERTIES = "skins.properties";

  private Skin defaultSkin;

  public List<SkinMapping> setupSkins(Configuration configuration, String branch, CacheManager cacheManager)
      throws BranchNotFoundException, IOException, ContentManagerException, FileNotFoundException, UnsupportedEncodingException {
    List<SkinMapping> skinMappings;
    defaultSkin = registerBuiltinSkins(branch);
    registerLocalSkins(branch);
    skinMappings = new ArrayList<>();

    String branchFolder = ContentManagerFactory.getContentManager().getBranchFolder(branch);
    String skinMappingFileName = branchFolder + SKINS_PROPERTIES;
    File skinMappingFile = new File(skinMappingFileName);
    if (!skinMappingFile.isFile()) {
      LOG.warn("No " + SKINS_PROPERTIES + " properties file found at " + skinMappingFileName + " so falling back to built in which is "
          + configuration.getDefaultSkin());
      skinMappings.add(new SkinMapping(Pattern.compile(".*"), defaultSkin));
    } else {
      skinMappings.addAll(createSkinMappingsFromFile(branch, skinMappingFileName));
    }
    cacheManager.registerSkinMappings(skinMappings);

    setupInheritance(cacheManager);
    return skinMappings;
  }

  protected void setupInheritance(CacheManager cacheManager) {

    for (Skin skin : cacheManager.getSkins()) {
      String inheritsFrom = skin.getInheritsFrom();
      if (StringUtils.isNotBlank(inheritsFrom)) {
        Skin superSkin = cacheManager.getSkinByName(inheritsFrom);
        if (superSkin == null)
          LOG.error("Skin with name " + inheritsFrom //
              + " not defined. Check skin.properties of Skin defined by " //
              + skin.getPropertyFileName());
        else {
          skin.setSuper(superSkin);
          cacheManager.registerSkinInheritance(new SkinInheritance(skin, superSkin));
        }
      }
    }
  }

  /**
   * Returns the default (fallback) skin. Only valid after calling setupSkins.
   *
   * @return
   */
  public Skin getDefaultSkin() {
    return defaultSkin;
  }

  protected void registerLocalSkins(String branch) throws BranchNotFoundException, IOException, ContentManagerException {
    List<String> skinDescriptors = new ArrayList<>();
    for (ContentNode node : ContentManagerFactory.getContentManager().find(branch, "skin.properties", true))
      skinDescriptors.add(node.getPath());

    CacheManager cacheManager = CacheManager.getInstance(branch);
    createSkins(cacheManager, branch, skinDescriptors, false);
  }

  /**
   * Returns the Builtin skin
   */
  protected Skin registerBuiltinSkins(String branch) {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(BUILTIN_SKIN_LIST);
    if (is == null)
      throw new IllegalArgumentException("Builtin skin definition file " + BUILTIN_SKIN_LIST + " not found!");
    List<String> skinDescriptors = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      String line = br.readLine();
      while (line != null) {
        if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#")) {
          skinDescriptors.add(ThothUtil.getFolder(BUILTIN_SKIN_LIST) + "/" + line.trim() + "/" + Configuration.SKIN_PROPERTIES);
        }
        line = br.readLine();
      }
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    CacheManager cacheManager = CacheManager.getInstance(branch);
    Skin fallbackSkin = createSkins(cacheManager, branch, skinDescriptors, true);
    String defaultSkinName = Configuration.getInstance().getDefaultSkin();
    Skin defaultSkin = cacheManager.getSkinByName(defaultSkinName);
    if (defaultSkin == null) {
      LOG.error("Default skin named '" + defaultSkinName + "' not found. Falling back on first available which is " + fallbackSkin);
      defaultSkin = fallbackSkin;
    }
    return defaultSkin;
  }

  protected Skin createSkins(CacheManager cacheManager, String branch, List<String> skinDescriptors, boolean fromClasspath) {
    Skin fallbackSkin = null;
    for (String skinDescriptor : skinDescriptors) {
      try {
        Skin skin = new Skin(branch, (fromClasspath ? Configuration.CLASSPATH_PREFIX : "") + skinDescriptor);
        fallbackSkin = skin;
        cacheManager.registerSkin(skin);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
    return fallbackSkin;
  }

  protected List<SkinMapping> createSkinMappingsFromFile(String branch, String skinMappingFileName)
      throws FileNotFoundException, IOException, BranchNotFoundException, ContentManagerException, UnsupportedEncodingException {
    List<SkinMapping> skinMappings = new ArrayList<>();
    CacheManager cacheManager = CacheManager.getInstance(branch);
    InputStream is = new FileInputStream(skinMappingFileName);
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
      String line = br.readLine();
      while (line != null) {
        int idx = line.indexOf('=');
        if (!line.startsWith("#") && idx != -1) {
          String patternSpec = line.substring(0, idx).trim();
          String skinName = line.substring(idx + 1).trim();
          Pattern pattern = Pattern.compile(ThothUtil.fileSpec2regExp(patternSpec));
          Skin skin = cacheManager.getSkinByName(skinName);
          if (skin == null)
            LOG.error("Skin with name " + skinName + " not found. Mapping " + line + " ignored");
          skinMappings.add(new SkinMapping(pattern, skin));
        }
        line = br.readLine();
      }
    }
    return skinMappings;
  }

  protected Skin createAndRegisterSkin(String branch, String skinFileName) throws BranchNotFoundException, ContentManagerException {
    Skin skin = new Skin(branch, skinFileName);
    CacheManager instance = CacheManager.getInstance(branch);
    instance.registerSkin(skin);
    return skin;
  }

  public String getInheritedPath(String path, String branch) throws IOException, ContentManagerException {
    String result = null;
    String inheritedPath = handleBranchBasedInheritance(branch, path);
    if (inheritedPath != null)
      if (inheritedPath.startsWith(Configuration.CLASSPATH_PREFIX))
        result = inheritedPath;
      else
        result = ContentManagerFactory.getContentManager().getFileSystemPath(branch, inheritedPath);
    return result;
  }

  protected String handleBranchBasedInheritance(String branch, String path) {
    String result = null;

    CacheManager cacheManager = CacheManager.getInstance(branch);
    SkinInheritance skinInheritance = cacheManager.getSkinInheritance(path);
    if (skinInheritance != null) {
      String baseFolder = ThothUtil.stripPrefix(skinInheritance.getChild().getSkinBaseFolder(), "/");
      String remainder = path.substring(baseFolder.length());
      Skin parent = skinInheritance.getParent();
      result = parent.getSkinBaseFolder() + remainder;
    }
    return result;
  }

}
