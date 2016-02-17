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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.exceptions.CachemanagerException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.util.LineInfo;
import net.riezebos.thoth.markdown.util.ProcessorError;

public class CacheManager {
  private static Object fileLock = new Object();

  private Map<String, Map<String, List<String>>> reverseIndexes = new HashMap<>();
  private Map<String, List<ProcessorError>> errorMap = new HashMap<>();
  private String context;

  protected CacheManager(String context) {
    this.setContext(context);
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
  public Map<String, List<String>> getReverseIndex(boolean indirect) throws CachemanagerException {
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
    } catch (IOException | ClassNotFoundException | ContextNotFoundException e) {
      throw new CachemanagerException(getContext());
    }
    return map;
  }

  public void persistIndexingContext(IndexingContext indexingContext) throws CachemanagerException {
    try {
      ContentManager contentManager = getContentManager(context);
      String reverseIndexFile = contentManager.getReverseIndexFileName();
      String indirectReverseIndexFile = contentManager.getReverseIndexIndirectFileName();
      String errorFile = contentManager.getErrorFileName();

      synchronized (CacheManager.getFileLock()) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(reverseIndexFile)))) {
          oos.writeObject(indexingContext.getDirectReverseIndex());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(indirectReverseIndexFile)))) {
          oos.writeObject(indexingContext.getIndirectReverseIndex());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(errorFile)))) {
          oos.writeObject(indexingContext.getErrors());
        }
      }
    } catch (Exception e) {
      throw new CachemanagerException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public List<ProcessorError> getValidationErrors() throws CachemanagerException {
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
      throw new CachemanagerException(getContext() + ": " + e.getMessage(), e);
    }
    return errors;
  }

  public ContentManager getContentManager(String context) throws CachemanagerException {
    try {
      return ContentManagerFactory.getContentManager(context);
    } catch (ContentManagerException e) {
      throw new CachemanagerException(e);
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
