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
package net.riezebos.thoth.markdown.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.riezebos.thoth.util.ThothUtil;

public class DocumentNode {
  private String path;
  private String description;
  private String fileName;
  private String folder;
  private int includePosition;
  private int level;
  private List<DocumentNode> children = new ArrayList<DocumentNode>();

  public DocumentNode(String absolutefilePath, String description, int includePosition, int level) {
    String path = ThothUtil.normalSlashes(absolutefilePath);
    if (!path.startsWith("/"))
      path = "/" + path;

    // Take off the bookmark part of a link; if it is there
    int idx = path.indexOf("#");
    if (idx != -1)
      path = path.substring(0, idx);

    this.path = path;
    this.description = (description == null || description.isEmpty()) ? ThothUtil.getFileName(path) : description;
    this.includePosition = includePosition;
    this.fileName = ThothUtil.getFileName(path);
    this.folder = ThothUtil.getFolder(path);
    this.level = level;
  }

  public int getLevel() {
    return level;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getIncludePosition() {
    return includePosition;
  }

  public void setIncludePosition(int includePosition) {
    this.includePosition = includePosition;
  }

  public void addChild(DocumentNode includeUsage) {
    children.add(includeUsage);
  }

  public List<DocumentNode> getChildren() {
    return children;
  }

  public String getFileName() {
    return fileName;
  }

  public String getFolder() {
    return folder;
  }

  @Override
  public String toString() {
    return toString(0);
  }

  public String getDescription() {
    return description;
  }

  public String toString(int level) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < level; i++)
      sb.append("  ");
    sb.append(getPath());
    sb.append("\n");
    for (DocumentNode child : getChildren()) {
      sb.append(child.toString(level + 1));
      sb.append("\n");
    }
    return ThothUtil.stripSuffix(sb.toString(), "\n");
  }

  /**
   * Returns the document structure as a list of nodes by flattening the tree.
   *
   * @param removeDuplicates when true then nodes with the same pas will only appear once in the result
   * @return
   */
  public List<DocumentNode> flatten(boolean removeDuplicates) {
    List<DocumentNode> result = new ArrayList<DocumentNode>();
    visit(this, result);
    if (removeDuplicates) {
      Set<String> paths = new HashSet<String>();

      List<DocumentNode> nodups = new ArrayList<DocumentNode>();
      for (DocumentNode node : result) {
        if (!paths.contains(node.getPath()))
          nodups.add(node);
        paths.add(node.getPath());
      }
      result = nodups;
    }
    return result;
  }

  protected void visit(DocumentNode documentNode, List<DocumentNode> result) {
    result.add(documentNode);
    for (DocumentNode child : documentNode.getChildren()) {
      visit(child, result);
    }
  }
}
