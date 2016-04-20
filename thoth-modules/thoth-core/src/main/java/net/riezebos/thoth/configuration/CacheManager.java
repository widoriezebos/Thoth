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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.search.IndexingContext;
import net.riezebos.thoth.exceptions.CachemanagerException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.util.LineInfo;
import net.riezebos.thoth.markdown.util.ProcessorError;

public class CacheManager {
  private Object fileLock = new Object();

  private Map<String, Map<String, List<String>>> reverseIndexes = new HashMap<>();
  private Map<String, List<ProcessorError>> errorMap = new HashMap<>();
  private ContentManager contentManager;
  private List<Book> books = null;
  private List<String> allPaths = null;

  public CacheManager(ContentManager contentManager) {
    this.contentManager = contentManager;
  }

  public Object getFileLock() {
    return fileLock;
  }

  protected InputStream createInputStream(String resourcePath) throws FileNotFoundException {
    return new FileInputStream(resourcePath);
  }

  protected OutputStream createOutputStream(String resourcePath) throws FileNotFoundException {
    return new FileOutputStream(new File(resourcePath));
  }

  protected void deleteFile(String fileName) {
    File file = new File(fileName);
    file.delete();
  }

  /**
   * Returns a 'used by' map for documents. Returns null when indexing has not yet completed (ever) for the given context.
   *
   * @param contextName
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
        String indexFileName = indirect ? getContentManager().getReverseIndexIndirectFileName()//
            : getContentManager().getReverseIndexFileName();

        synchronized (fileLock) {
          InputStream inputStream = createInputStream(indexFileName);
          if (inputStream != null)
            try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
              map = (Map<String, List<String>>) ois.readObject();
              cacheReverseIndex(indirect, map);
            }
        }
      }
    } catch (IOException | ClassNotFoundException | ContextNotFoundException e) {
      throw new CachemanagerException(getContextName());
    }
    return map;
  }

  public void persistIndexingContext(IndexingContext indexingContext) throws CachemanagerException {
    try {
      ContentManager contentManager = getContentManager();
      String reverseIndexFile = contentManager.getReverseIndexFileName();
      String indirectReverseIndexFile = contentManager.getReverseIndexIndirectFileName();
      String errorFile = contentManager.getErrorFileName();

      synchronized (getFileLock()) {
        try (ObjectOutputStream oos = new ObjectOutputStream(createOutputStream(reverseIndexFile))) {
          oos.writeObject(indexingContext.getDirectReverseIndex());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(createOutputStream(indirectReverseIndexFile))) {
          oos.writeObject(indexingContext.getIndirectReverseIndex());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(createOutputStream(errorFile))) {
          ArrayList<ProcessorError> list = new ArrayList<>(indexingContext.getErrors());
          Collections.sort(list);
          oos.writeObject(list);
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
      errors = errorMap.get(getContextName());
    }

    try {
      if (errors == null) {
        String errorFileName = getContentManager().getErrorFileName();

        synchronized (fileLock) {
          InputStream inputStream = createInputStream(errorFileName);
          if (inputStream != null)
            try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
              errors = (List<ProcessorError>) ois.readObject();
              cacheErrors(errors);
            } catch (ClassNotFoundException e) {
              // This is no doubt caused by loading an old version of the serialized object stream.
              // Let's ignore it and delete the file. Will be fine after the next re-index
              deleteFile(errorFileName);
              errors = new ArrayList<ProcessorError>();
              errors.add(new ProcessorError(new LineInfo(".", 0), "Error messages out of date. Please reindex"));
            }
        }
      }
    } catch (IOException | ContextNotFoundException e) {
      throw new CachemanagerException(getContextName() + ": " + e.getMessage(), e);
    }
    return errors;
  }

  protected String getCacheKey(boolean indirect) {
    String key = indirect + getContextName();
    return key;
  }

  public void cacheReverseIndex(boolean indirect, Map<String, List<String>> reverseIndex) {
    String key = getCacheKey(indirect);
    synchronized (reverseIndexes) {
      reverseIndexes.put(key, reverseIndex);
    }
  }

  public List<String> getAllPaths() {
    return allPaths;
  }

  public void setAllPaths(List<String> allPaths) {
    this.allPaths = allPaths;
  }

  public List<Book> getBooks() {
    return books;
  }

  public void setBooks(List<Book> books) {
    this.books = books;
  }

  public void cacheErrors(List<ProcessorError> errors) {
    synchronized (errorMap) {
      errorMap.put(getContextName(), errors);
    }
  }

  private String getContextName() {
    return getContentManager().getContextName();
  }

  public ContentManager getContentManager() {
    return contentManager;
  }

  @Override
  public String toString() {
    return "Cache for " + getContextName();
  }
}
