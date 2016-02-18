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

import java.io.Serializable;

public class LineInfo implements Cloneable, Serializable {
  private static final long serialVersionUID = 1L;
  private String file;
  private int line;

  public LineInfo(String file, int line) {
    super();
    setFile(file);
    this.line = line;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    if (!file.startsWith("/"))
      file = "/" + file;
    this.file = file;
  }

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  @Override
  public String toString() {
    return file + "(" + getLine() + ")";
  }

  @Override
  public LineInfo clone() {
    try {
      return (LineInfo) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((file == null) ? 0 : file.hashCode());
    result = prime * result + line;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LineInfo other = (LineInfo) obj;
    if (file == null) {
      if (other.file != null)
        return false;
    } else if (!file.equals(other.file))
      return false;
    if (line != other.line)
      return false;
    return true;
  }
}
