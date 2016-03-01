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
package net.riezebos.thoth.configuration.persistence.dbs.impl;

public class TranslationPair {
  private String from;
  private String to;
  private String excludeMatch;

  public TranslationPair(String from, String to) {
    super();
    this.from = from;
    this.to = to;
  }

  public TranslationPair(String from, String to, String excludeMatch) {
    this(from, to);
    this.excludeMatch = excludeMatch;
  }

  public boolean exclude(String command) {
    if (getExcludeMatch() == null)
      return false;
    String[] excludes = getExcludeMatch().split("\\,");
    for (String exclude : excludes)
      if (command.indexOf(exclude) != -1)
        return true;
    return false;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public String getExcludeMatch() {
    return excludeMatch;
  }
}
