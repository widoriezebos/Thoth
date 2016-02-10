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

import net.riezebos.thoth.markdown.util.LineInfo;

public class BookmarkUsage {

  private LineInfo currentLineInfo;
  private String bookmark;

  public BookmarkUsage() {
  }

  public BookmarkUsage(String bookmark) {
    this.bookmark = bookmark;
  }

  public void setCurrentLineInfo(LineInfo currentLineInfo) {
    this.currentLineInfo = currentLineInfo.clone();
  }

  public LineInfo getCurrentLineInfo() {
    return currentLineInfo;
  }

  public void setBookMark(String bookmark) {
    this.bookmark = bookmark;
  }

  public String getBookmark() {
    return bookmark;
  }

  @Override
  public String toString() {
    return currentLineInfo + ": #" + getBookmark();
  }
}
