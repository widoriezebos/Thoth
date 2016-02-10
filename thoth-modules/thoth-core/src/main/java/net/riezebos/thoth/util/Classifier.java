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
package net.riezebos.thoth.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.beans.BookClassification;

/**
 * @author wido
 */
public class Classifier {
  private static final String FOLDER_META_TAG = "folder";

  public List<BookClassification> getClassifications(List<Book> books, String metaTagName, String defaultValue) {
    Map<String, BookClassification> classificationMap = new HashMap<>();

    for (Book book : books) {

      String classificationSpec = book.getMetaTag(metaTagName);

      if (classificationSpec == null) {
        // Special case for meta tag 'folder'
        if (FOLDER_META_TAG.equalsIgnoreCase(metaTagName)) {
          classificationSpec = book.getFolder();
          if (StringUtils.isBlank(classificationSpec))
            classificationSpec = "/";
        } else
          classificationSpec = defaultValue;
      }

      for (String classificationName : ThothUtil.sort(ThothUtil.tokenize(classificationSpec))) {
        BookClassification classification = classificationMap.get(classificationName);
        if (classification == null) {
          classification = new BookClassification(classificationName);
          classificationMap.put(classificationName, classification);
        }
        classification.getBooks().add(book);
      }
    }

    for (BookClassification classification : classificationMap.values())
      classification.sortBooks();

    List<BookClassification> result = new ArrayList<>();
    result.addAll(classificationMap.values());
    Collections.sort(result);
    return result;
  }

}
