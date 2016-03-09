package net.riezebos.thoth.content;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.riezebos.thoth.util.ThothUtil;

public class AccessRule {
  private Pattern pathPattern;
  private Set<String> requires = new HashSet<>();
  private boolean matchAll;

  public AccessRule(String pathPatternSpec, String groupSpec, boolean matchAll) {

    this.matchAll = matchAll;
    // let's avoid hassle if somebody specified an absolute path in the rules.
    // Paths we will have to match are relative to the filesystem root i.e. no '/' prefix
    String absPathSpec = ThothUtil.stripPrefix(pathPatternSpec, "/");

    pathPattern = Pattern.compile("^" + ThothUtil.fileSpec2regExp(absPathSpec) + "$");
    String commaSeparated = groupSpec.replaceAll("\\;\\:", ",");
    for (String group : commaSeparated.split(",")) {
      requires.add(group.trim());
    }
  }

  public boolean applies(String path) {
    // Note that we will remove any trailing '/' from the path before we match. Also see constructor above
    return pathPattern.matcher(ThothUtil.stripPrefix(path, "/")).matches();
  }

  public boolean isAccessAllowed(Set<String> userGroups) {
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
