/* Copyright (c) 2020 W.T.J. Riezebos
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

public class Bookmark {
  private int level;
  private String id;
  private String title;
  private boolean useForToc;

  public Bookmark(int level, String id, String title, boolean useForToc) {
    this.level = level;
    this.id = id;
    this.title = title.trim();
    this.useForToc = useForToc;
  }

  public boolean isUseForToc() {
    return useForToc;
  }

  public int getLevel() {
    return level;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    return getTitle();
  }
}
