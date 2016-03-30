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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Book implements Comparable<Book> {
  private String name;
  private String path;
  private String folder;
  private String title;
  private Map<String, String> metaTags = new HashMap<String, String>();

  public Book(String name, String path) {
    super();
    setName(name);
    setPath(path);
    setFolder(path);
    setTitle(name);
  }

  private void setFolder(String path) {
    int idx = path.replaceAll("\\\\", "/").lastIndexOf("/");
    if (idx != -1)
      path = path.substring(0, idx);
    folder = path;
  }

  private void setTitle(String name) {
    title = name;
    int idx = title.lastIndexOf(".");
    if (idx != -1)
      title = title.substring(0, idx);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getTitle() {
    return title;
  }

  public String getFolder() {
    return folder;
  }

  @Override
  public int compareTo(Book o) {
    return getName().compareTo(o.getName());
  }

  public void setMetaTags(Map<String, String> metaTags) {
    this.metaTags = metaTags;
  }

  public Map<String, String> getMetaTags() {
    return metaTags;
  }

  public List<String> getMetaTagKeys() {
    List<String> keys = new ArrayList<String>(metaTags.keySet());
    Collections.sort(keys);
    return keys;
  }

  public String getMetaTag(String key) {
    return getMetaTags().get(key);
  }

  @Override
  public String toString() {
    return getPath();
  }
}
