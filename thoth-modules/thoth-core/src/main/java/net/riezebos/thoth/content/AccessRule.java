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
package net.riezebos.thoth.content;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.riezebos.thoth.util.ThothUtil;

public class AccessRule {
  private Pattern pathPattern;
  private Set<String> requires = new HashSet<>();
  private boolean matchAll;
  private boolean requireNone;

  public AccessRule(String pathPatternSpec, String groupSpec, boolean matchAll, boolean requireNone) {

    this.matchAll = matchAll;
    this.requireNone = requireNone;
    // let's avoid hassle if somebody specified an absolute path in the rules.
    // Paths we will have to match are relative to the filesystem root i.e. no '/' prefix
    String absPathSpec = ThothUtil.stripPrefix(pathPatternSpec, "/");

    pathPattern = Pattern.compile("^" + ThothUtil.fileSpec2regExp(absPathSpec) + "$");
    String commaSeparated = groupSpec.replaceAll("[\\;\\:]", ",");
    for (String group : commaSeparated.split(",")) {
      requires.add(group.trim());
    }
  }

  public boolean applies(String path) {
    // Note that we will remove any trailing '/' from the path before we match. Also see constructor above
    return pathPattern.matcher(ThothUtil.stripPrefix(path, "/")).matches();
  }

  public boolean isAccessAllowed(Set<String> userGroups) {
    if (requireNone)
      return true;

    if (matchAll)
      return userGroups.containsAll(requires);
    for (String group : userGroups)
      if (requires.contains(group))
        return true;
    return false;
  }

  @Override
  public String toString() {
    return pathPattern.toString() + " requires " + requires;
  }
}
