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
package net.riezebos.thoth.markdown.util;

import java.util.regex.Pattern;

import net.riezebos.thoth.util.ThothUtil;

public class SoftLinkTranslation {

  Pattern pattern;
  private String replacePattern;

  public SoftLinkTranslation(String patternSpec, String replacePattern) {
    pattern = Pattern.compile("^" + ThothUtil.fileSpec2regExp(patternSpec) + "$");
    this.replacePattern = replacePattern;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public String getReplacePattern() {
    return replacePattern;
  }

  @Override
  public String toString() {
    return getPattern().toString();
  }
}
