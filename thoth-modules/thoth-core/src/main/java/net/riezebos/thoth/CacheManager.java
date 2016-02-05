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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.skinning.SkinInheritance;
import net.riezebos.thoth.content.skinning.SkinMapping;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.IndexerException;
import net.riezebos.thoth.markdown.util.LineInfo;
import net.riezebos.thoth.markdown.util.ProcessorError;
import net.riezebos.thoth.util.ThothUtil;

public class CacheManager {
  private static final Logger LOG = LoggerFactory.getLogger(CacheManager.class);
  private static final String GLOBAL_SITE = "*global_site*";

  private static Map<String, CacheManager> instances = new HashMap<>();
  private static Object fileLock = new Object();

  private List<SkinMapping> skinMappings = null;
  private Map<String, SkinInheritance> skinInheritances = new HashMap<>();
  private Map<String, Skin> skinsByName = new HashMap<>();
  private List<Skin> skins = new ArrayList<>();
  private Map<String, Map<String, List<String>>> reverseIndexes = new HashMap<>();
  private Map<String, List<ProcessorError>> errorMap = new HashMap<>();
  private String branch;

  public CacheManager(String branch) {
    this.branch = branch;
  }

  public static CacheManager getInstance(String branch) {
    if (branch == null)
      branch = GLOBAL_SITE;
    CacheManager cacheManager;
    synchronized (instances) {
      cacheManager = instances.get(branch);
    }
    if (cacheManager == null) {
      cacheManager = new CacheManager(branch);
      synchronized (instances) {
        instances.put(branch, cacheManager);
      }
    }
    return cacheManager;
  }

  public static void expire(String branch) {
    if (branch == null)
      branch = GLOBAL_SITE;
    synchronized (instances) {
      instances.remove(branch);
      // We do not know where the global site is getting it's data from so expire that one as well just to be safe
      instances.remove(GLOBAL_SITE);
    }
  }

  public static Object getFileLock() {
    return fileLock;
  }

  /**
   * Returns a 'used by' map for documents. Returns null when indexing has not yet completed (ever) for the given branch.
   *
   * @param branch
   * @return
   * @throws BranchNotFoundException
   * @throws ContentManagerException
   */
  @SuppressWarnings("unchecked")
  public Map<String, List<String>> getReverseIndex(boolean indirect) throws BranchNotFoundException, ContentManagerException {
    String key = getCacheKey(indirect);
    Map<String, List<String>> map;

    synchronized (reverseIndexes) {
      map = reverseIndexes.get(key);
    }

    try {
      if (map == null) {
        String indexFileName = indirect ? getContentManager().getReverseIndexIndirectFileName(branch)//
            : getContentManager().getReverseIndexFileName(branch);

        File reverseIndexFile = new File(indexFileName);
        if (reverseIndexFile.isFile()) {
          synchronized (fileLock) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(reverseIndexFile))) {
              map = (Map<String, List<String>>) ois.readObject();
              cacheReverseIndex(indirect, map);
            }
          }
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new IndexerException(branch);
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  public List<ProcessorError> getValidationErrors() throws IndexerException {
    List<ProcessorError> errors;
    synchronized (errorMap) {
      errors = errorMap.get(branch);
    }

    try {
      if (errors == null) {
        String errorFileName = getContentManager().getErrorFileName(branch);

        File errorFile = new File(errorFileName);
        if (errorFile.isFile()) {
          synchronized (fileLock) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(errorFile))) {
              errors = (List<ProcessorError>) ois.readObject();
              cacheErrors(errors);
            } catch (ClassNotFoundException e) {
              // This is no doubt caused by loading an old version of the serialized object stream.
              // Let's ignore it and delete the file. Will be fine after the next re-index
              errorFile.delete();
              errors = new ArrayList<ProcessorError>();
              errors.add(new ProcessorError(new LineInfo(".", 0), "Error messages out of date. Please reindex"));
            }
          }
        }
      }
    } catch (IOException | BranchNotFoundException e) {
      throw new IndexerException(branch + ": " + e.getMessage(), e);
    }
    return errors;
  }

  public ContentManager getContentManager() throws IndexerException {
    try {
      return ContentManagerFactory.getContentManager();
    } catch (ContentManagerException e) {
      throw new IndexerException(e);
    }
  }

  protected String getCacheKey(boolean indirect) {
    String key = indirect + branch;
    return key;
  }

  public void cacheReverseIndex(boolean indirect, Map<String, List<String>> reverseIndex) {
    String key = getCacheKey(indirect);
    synchronized (reverseIndexes) {
      reverseIndexes.put(key, reverseIndex);
    }
  }

  public void cacheErrors(List<ProcessorError> errors) {
    synchronized (errorMap) {
      errorMap.put(branch, errors);
    }
  }

  public void registerSkinMappings(List<SkinMapping> skinMappings) {
    this.skinMappings = skinMappings;
  }

  public void registerSkin(Skin skin) {
    synchronized (skinsByName) {
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
  }

  public Skin getSkinByName(String name) {
    if (name == null)
      return null;
    synchronized (skinsByName) {
      return skinsByName == null ? null : skinsByName.get(name.toLowerCase());
    }
  }

  public List<SkinMapping> getSkinMappings() {
    return skinMappings;
  }

  public List<Skin> getSkins() {
    return skins;
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
}
