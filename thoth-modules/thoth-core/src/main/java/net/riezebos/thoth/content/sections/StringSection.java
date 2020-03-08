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
package net.riezebos.thoth.content.sections;

public class StringSection extends Section {

  private StringBuilder text;

  public StringSection(String text) {
    super();
    this.text = new StringBuilder(text);
  }

  @Override
  public boolean isFlatText() {
    return true;
  }

  @Override
  public String getPath() {
    Section parent = getParent();
    if (parent != null)
      return parent.getPath();
    else
      return super.getPath();
  }

  @Override
  public String toString() {
    return text.toString();
  }

  public void append(String additional) {
    text.append("\n");
    text.append(additional);
  }

  @Override
  public String getLocalText() {
    return text.toString();
  }

}
