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
package net.riezebos.thoth.content.skinning;

import java.util.regex.Pattern;

import net.riezebos.thoth.util.ThothUtil;

public class SkinMapping {
  private Pattern pattern;
  private Skin skin;

  public SkinMapping(String patternSpec, Skin skin) {
    setPattern(Pattern.compile(ThothUtil.fileSpec2regExp(patternSpec)));
    setSkin(skin);
  }

  public Pattern getPattern() {
    return pattern;
  }

  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  public Skin getSkin() {
    return skin;
  }

  public void setSkin(Skin skin) {
    this.skin = skin;
  }

  @Override
  public String toString() {
    return pattern + " " + skin;
  }
}
