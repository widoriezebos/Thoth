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
package net.riezebos.thoth.beans;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.markdown.util.ProcessorError;

/**
 * Contains all information about a MarkDown document, including meta tags, document structure and any errors that arose during include and link processing.
 * 
 * @author wido
 */
public class MarkDownDocument {
  private String markdown;
  private Map<String, String> metatags = new HashMap<String, String>();
  private List<ProcessorError> errors = new ArrayList<>();
  private DocumentNode documentStructure;

  public MarkDownDocument(String markdown, Map<String, String> metatags, List<ProcessorError> errors, DocumentNode documentStructure) {
    super();
    this.markdown = markdown;
    this.metatags = metatags;
    this.errors = errors;
    this.documentStructure = documentStructure;
  }

  public DocumentNode getDocumentStructure() {
    return documentStructure;
  }

  public String getMarkdown() {
    return markdown;
  }

  public Map<String, String> getMetatags() {
    return metatags;
  }

  public String getMetatag(String key) {
    return metatags.get(key);
  }

  public boolean hasErrors() {
    return !this.errors.isEmpty();
  }

  public List<ProcessorError> getErrors() {
    return errors;
  }

  public String getPath() {
    if (documentStructure != null)
      return documentStructure.getPath();
    else
      return null;
  }

  public String getName() {
    if (documentStructure != null)
      return documentStructure.getFileName();
    else
      return null;
  }

  public String getTitle() {
    String title = getMetatag("title");
    if (title == null)
      return getName();
    else
      return title;
  }

  public Reader getReader() {
    try {
      byte[] bytes = this.markdown.getBytes("UTF-8");
      return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
