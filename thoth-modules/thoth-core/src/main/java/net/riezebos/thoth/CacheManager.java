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

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.IndexerException;
import net.riezebos.thoth.markdown.util.LineInfo;
import net.riezebos.thoth.markdown.util.ProcessorError;

public class CacheManager {
  private static final String GLOBAL_SITE = "*global_site*";

  private static Map<String, CacheManager> instances = new HashMap<>();
  private static Object fileLock = new Object();

  private Map<String, Map<String, List<String>>> reverseIndexes = new HashMap<>();
  private Map<String, List<ProcessorError>> errorMap = new HashMap<>();
  private String context;

  public CacheManager(String context) {
    this.setContext(context);
  }

  public static CacheManager getInstance(String context) {
    if (context == null)
      context = GLOBAL_SITE;
    CacheManager cacheManager;
    synchronized (instances) {
      cacheManager = instances.get(context);
    }
    if (cacheManager == null) {
      cacheManager = new CacheManager(context);
      synchronized (instances) {
        instances.put(context, cacheManager);
      }
    }
    return cacheManager;
  }

  public static void expire(String context) {
    if (context == null)
      context = GLOBAL_SITE;
    synchronized (instances) {
      instances.remove(context);
      // We do not know where the global site is getting it's data from so expire that one as well just to be safe
      instances.remove(GLOBAL_SITE);
    }
  }

  public static Object getFileLock() {
    return fileLock;
  }

  /**
   * Returns a 'used by' map for documents. Returns null when indexing has not yet completed (ever) for the given context.
   *
   * @param context
   * @return
   * @throws ContextNotFoundException
   * @throws ContentManagerException
   */
  @SuppressWarnings("unchecked")
  public Map<String, List<String>> getReverseIndex(boolean indirect) throws ContextNotFoundException, ContentManagerException {
    String key = getCacheKey(indirect);
    Map<String, List<String>> map;

    synchronized (reverseIndexes) {
      map = reverseIndexes.get(key);
    }

    try {
      if (map == null) {
        String indexFileName = indirect ? getContentManager(getContext()).getReverseIndexIndirectFileName()//
            : getContentManager(getContext()).getReverseIndexFileName();

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
      throw new IndexerException(getContext());
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  public List<ProcessorError> getValidationErrors() throws IndexerException {
    List<ProcessorError> errors;
    synchronized (errorMap) {
      errors = errorMap.get(getContext());
    }

    try {
      if (errors == null) {
        String errorFileName = getContentManager(getContext()).getErrorFileName();

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
    } catch (IOException | ContextNotFoundException e) {
      throw new IndexerException(getContext() + ": " + e.getMessage(), e);
    }
    return errors;
  }

  public ContentManager getContentManager(String context) throws IndexerException {
    try {
      return ContentManagerFactory.getContentManager(context);
    } catch (ContentManagerException e) {
      throw new IndexerException(e);
    }
  }

  protected String getCacheKey(boolean indirect) {
    String key = indirect + getContext();
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
      errorMap.put(getContext(), errors);
    }
  }

  private String getContext() {
    return context;
  }

  private void setContext(String context) {
    this.context = context;
  }

  @Override
  public String toString() {
    return "Cache for " + getContext();
  }
}
