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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.content.SkinMapping;
import net.riezebos.thoth.content.markdown.util.ProcessorError;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.IndexerException;

public class CacheManager {
  private static final String GLOBAL_SITE = "*global_site*";

  private static Map<String, CacheManager> instances = new HashMap<>();
  private static Object fileLock = new Object();

  private List<SkinMapping> skinMappings = null;
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
            }
          }
        }
      }
    } catch (IOException | ClassNotFoundException | BranchNotFoundException e) {
      throw new IndexerException(branch);
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

  public List<SkinMapping> getSkinMappings() {
    return skinMappings;
  }
}
