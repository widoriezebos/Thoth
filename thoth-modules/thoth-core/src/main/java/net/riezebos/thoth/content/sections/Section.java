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
package net.riezebos.thoth.content.sections;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.riezebos.thoth.content.comments.Comment;

public class Section {
  private List<Section> subSections = new ArrayList<>();
  private List<Comment> comments = new ArrayList<>();
  private String path = null;
  private Section parent;

  public Section(String path) {
    this.path = path;
  }

  public Section() {
  }

  public boolean isFlatText() {
    return false;
  }

  public String getPath() {
    return path;
  }

  public void addSection(String text) {
    boolean handled = false;
    if (!subSections.isEmpty()) {
      Section latest = subSections.get(subSections.size() - 1);
      if (latest instanceof StringSection) {
        ((StringSection) latest).append(text);
        handled = true;
      }
    }
    if (!handled)
      addSection(new StringSection(text));
  }

  public void addSection(Section section) {
    subSections.add(section);
    section.setParent(this);
  }

  public List<Section> getSubSections() {
    return subSections;
  }

  protected void setParent(Section section) {
    parent = section;
  }

  @JsonIgnore
  public Section getParent() {
    return parent;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    subSections.stream().forEach(s -> sb.append(s.toString()));
    return sb.toString();
  }

  public String getLocalText() {
    return "";
  }

  public List<Comment> getComments() {
    return comments;
  }

  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }
}
