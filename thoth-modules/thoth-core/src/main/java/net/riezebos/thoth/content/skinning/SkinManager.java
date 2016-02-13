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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.util.ThothUtil;

public class SkinManager {

  private static final Logger LOG = LoggerFactory.getLogger(SkinManager.class);
  public static final String SKIN_PARENT_OF_ALL = "SimpleSkin";
  private static final String BUILTIN_SKIN_LIST = "net/riezebos/thoth/skins/builtinskins.txt";
  private static final String SKINS_PROPERTIES = "skins.properties";

  private List<SkinMapping> skinMappings = new ArrayList<>();
  private Map<String, Skin> skinsByName = new HashMap<>();
  private List<Skin> skins = new ArrayList<>();
  private Map<String, SkinInheritance> skinInheritances = new HashMap<>();
  private Skin defaultSkin;
  private String defaultSkinName;
  private ContentManager contentManager;

  public SkinManager(ContentManager contentManager, String defaultSkinName) throws SkinManagerException {
    if (defaultSkinName == null)
      defaultSkinName = SKIN_PARENT_OF_ALL;
    this.setContentManager(contentManager);
    this.defaultSkinName = defaultSkinName;
    try {
      String context = contentManager.getContext();

      List<Skin> allSkins = getBuiltinSkins(contentManager);
      allSkins.addAll(getLocalSkins(contentManager));
      Skin defaultSkin = determineDefaultSkin(allSkins);
      FileHandle skinMappingFile = contentManager.getFileHandle(SKINS_PROPERTIES);
      if (!skinMappingFile.isFile()) {
        LOG.info(
            "No " + SKINS_PROPERTIES + " properties file found at " + skinMappingFile.getName() + " so falling back to default which is " + defaultSkinName);
        skinMappings.add(new SkinMapping("*", defaultSkin));
      } else {
        skinMappings.addAll(createSkinMappingsFromFile(context, skinMappingFile));
      }
      setupInheritance(allSkins);
    } catch (IOException | ContentManagerException e) {
      throw new SkinManagerException(e);
    }
  }

  protected void setupInheritance(List<Skin> allSkins) {
    for (Skin skin : allSkins) {
      String inheritsFrom = skin.getInheritsFrom();
      if (StringUtils.isNotBlank(inheritsFrom)) {
        Skin superSkin = getSkinByName(inheritsFrom);
        if (superSkin == null) {
          LOG.error("Skin with name " + inheritsFrom //
              + " not defined. Check skin.properties of Skin defined by " //
              + skin.getPropertyFileName());
          superSkin = getSkinByName(SKIN_PARENT_OF_ALL);
          if (superSkin == null)
            LOG.error("In trouble now: skin " + SKIN_PARENT_OF_ALL + " is not defined");
        }
        skin.setSuper(superSkin);
        registerSkinInheritance(new SkinInheritance(skin, superSkin));
      }
    }
  }

  protected List<Skin> getLocalSkins(ContentManager contentManager) throws ContextNotFoundException, IOException, ContentManagerException {
    List<String> skinDescriptors = new ArrayList<>();
    for (ContentNode node : contentManager.find("skin.properties", true))
      skinDescriptors.add(node.getPath());

    return createSkins(contentManager, skinDescriptors, false);
  }

  /**
   * Returns the Builtin skin
   */
  protected List<Skin> getBuiltinSkins(ContentManager contentManager) {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(BUILTIN_SKIN_LIST);
    if (is == null)
      throw new IllegalArgumentException("Builtin skin definition file " + BUILTIN_SKIN_LIST + " not found!");
    List<String> skinDescriptors = getSkinDescriptors(is);
    return createSkins(contentManager, skinDescriptors, true);
  }

  protected List<Skin> createSkins(ContentManager contentManager, List<String> skinDescriptors, boolean fromClasspath) {
    List<Skin> result = new ArrayList<>();
    for (String skinDescriptor : skinDescriptors) {
      try {
        Skin skin = new Skin(contentManager, (fromClasspath ? Configuration.CLASSPATH_PREFIX : "") + skinDescriptor);
        registerSkin(skin);
        result.add(skin);
      } catch (ContextNotFoundException e) {
        LOG.warn("Cannot create skin " + skinDescriptor + " for unknown context: " + contentManager.getContext());
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
    return result;
  }

  protected List<String> getSkinDescriptors(InputStream is) {
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
    return skinDescriptors;
  }

  protected List<SkinMapping> createSkinMappingsFromFile(String context, FileHandle skinMappingFile)
      throws FileNotFoundException, IOException, ContextNotFoundException, ContentManagerException, UnsupportedEncodingException {
    List<SkinMapping> skinMappings = new ArrayList<>();
    InputStream is = skinMappingFile.getInputStream();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
      String line = br.readLine();
      while (line != null) {
        int idx = line.indexOf('=');
        if (!line.startsWith("#") && idx != -1) {
          String patternSpec = line.substring(0, idx).trim();
          String skinName = line.substring(idx + 1).trim();
          Skin skin = getSkinByName(skinName);
          if (skin == null)
            LOG.error("Skin with name " + skinName + " not found. Mapping " + line + " ignored");
          else
            skinMappings.add(new SkinMapping(patternSpec, skin));
        }
        line = br.readLine();
      }
    }
    return skinMappings;
  }

  public String getInheritedPath(String path) throws IOException, ContentManagerException {
    String result = null;

    SkinInheritance skinInheritance = getSkinInheritance(path);
    if (skinInheritance != null) {
      String baseFolder = ThothUtil.stripPrefix(skinInheritance.getChild().getSkinBaseFolder(), "/");
      String remainder = path.substring(baseFolder.length());
      Skin parent = skinInheritance.getParent();
      result = parent.getSkinBaseFolder() + remainder;
    }
    return result;
  }

  public void registerSkinInheritance(SkinInheritance skinInheritance) {
    String key = ThothUtil.stripPrefix(skinInheritance.getChild().getSkinBaseFolder(), "/");
    skinInheritances.put(key, skinInheritance);
  }

  public SkinInheritance getSkinInheritance(String path) {
    for (Entry<String, SkinInheritance> entry : skinInheritances.entrySet())
      if (path.startsWith(entry.getKey()))
        return entry.getValue();
    return null;
  }

  protected Skin determineDefaultSkin(List<Skin> allSkins) {
    Skin result = null;
    for (Skin skin : allSkins)
      if (skin.getName().equalsIgnoreCase(defaultSkinName))
        result = skin;
    if (result == null) {
      LOG.warn("Default skin setting " + defaultSkinName + " is not a known in skin. Must be one of ");
      result = allSkins.get(0);
    }

    setDefaultSkin(result);
    return result;
  }

  public Skin getSkinByName(String name) {
    return skinsByName == null ? null : skinsByName.get(name.toLowerCase());
  }

  public List<SkinMapping> getSkinMappings() {
    return skinMappings;
  }

  public List<Skin> getSkins() {
    return skins;
  }

  protected void registerSkin(Skin skin) {
    String key = skin.getName().toLowerCase();
    Skin existing = skinsByName.get(key);
    if (existing != null) {
      LOG.warn("There are multiple skins with the name '" + skin.getName() //
          + "'. Found at " + skin.getPropertyFileName() + " and " + existing.getPropertyFileName());
    } else {
      skinsByName.put(key, skin);
      skins.add(skin);
    }
  }

  /**
   * Returns the default (fallback) skin.
   *
   * @return
   */
  public Skin getDefaultSkin() {
    return this.defaultSkin;
  }

  protected void setDefaultSkin(Skin defaultSkin) {
    this.defaultSkin = defaultSkin;
  }

  protected ContentManager getContentManager() {
    return contentManager;
  }

  private void setContentManager(ContentManager contentManager) {
    this.contentManager = contentManager;
  }
}
