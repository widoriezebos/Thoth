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
import java.util.Date;
import java.util.List;

import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.util.ThothUtil;

public class ContentNode implements Comparable<ContentNode> {
  private String path;
  private boolean isFolder;
  private Date dateModified;
  private long size;
  private List<ContentNode> children = new ArrayList<ContentNode>();

  public ContentNode(String path, FileHandle file) {
    super();
    setPath(path);
    setFolder(file.isDirectory());
    dateModified = new Date(file.lastModified());
    size = isFolder ? 0 : file.length();
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = ThothUtil.prefix(path, "/");
  }

  public boolean isFolder() {
    return isFolder;
  }

  public void setFolder(boolean isFolder) {
    this.isFolder = isFolder;
  }

  public List<ContentNode> getChildren() {
    return children;
  }

  public void setChildren(List<ContentNode> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    return getPath();
  }

  public Date getDateModified() {
    return dateModified;
  }

  public long getSize() {
    return size;
  }

  @Override
  public int compareTo(ContentNode o) {
    int result = new Boolean(isFolder).compareTo(o.isFolder);
    if (result != 0)
      return result;

    return getPath().compareTo(o.getPath());
  }
}
