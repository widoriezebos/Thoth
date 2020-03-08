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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.util.ThothUtil;

public class AccessManager {
  private static final Logger LOG = LoggerFactory.getLogger(AccessManager.class);
  private static final String ACCESS_RULES_FILE = "access.rules";

  private Pattern requireNonePattern = Pattern.compile("(.*?)\\s+requirenone\\s*", Pattern.CASE_INSENSITIVE);
  private Pattern requirePattern = Pattern.compile("(.*?)\\s+require\\s+(.*)", Pattern.CASE_INSENSITIVE);
  private Pattern requireAllPattern = Pattern.compile("(.*?)\\s+requireall\\s+(.*)", Pattern.CASE_INSENSITIVE);
  private Pattern requireAnyPattern = Pattern.compile("(.*?)\\s+requireany\\s+(.*)", Pattern.CASE_INSENSITIVE);
  private ContentManager contentManager;
  private List<AccessRule> accessRules = new ArrayList<>();
  private boolean denyall = false;

  public AccessManager(ContentManager contentManager) {
    this.contentManager = contentManager;
    FileHandle accessRulesFile = contentManager.getFileHandle(ThothUtil.suffix(contentManager.getLibraryRoot(), "/") + ACCESS_RULES_FILE);
    if (accessRulesFile.exists()) {
      loadAccessRules(accessRulesFile);
    } else {
      LOG.info("No access.rules file found at " + accessRulesFile.getAbsolutePath() + " so defaults apply");
    }
  }

  protected void loadAccessRules(FileHandle accessRulesFile) {
    try {
      String rules = ThothUtil.readInputStream(accessRulesFile.getInputStream());
      for (String rule : rules.split("\n")) {
        if (StringUtils.isNotBlank(rule) && !rule.trim().startsWith("#")) {
          boolean matched = false;
          Matcher matcher = requirePattern.matcher(rule);
          if (matcher.matches()) {
            matched = addRule(matcher, true, false);
          }
          matcher = requireAllPattern.matcher(rule);
          if (matcher.matches()) {
            matched = addRule(matcher, true, false);
          }
          matcher = requireAnyPattern.matcher(rule);
          if (matcher.matches()) {
            matched = addRule(matcher, false, false);
          }
          matcher = requireNonePattern.matcher(rule);
          if (matcher.matches()) {
            matched = addRule(matcher, false, true);
          }
          if (!matched)
            LOG.warn("Invalid rule, require / requireAll / requireAny keyword is missing in line: " + rule);
        }

      }
    } catch (Exception e) {
      denyall = true;
      LOG.error("As a consequence all access to " + contentManager.getContextName() + " will be DENIED");
    }
  }

  private boolean addRule(Matcher matcher, boolean matchAll, boolean requireNone) {
    String path = matcher.group(1).trim();
    String groups = "";
    if (!requireNone)
      groups = matcher.group(2).trim();
    accessRules.add(new AccessRule(path, groups, matchAll, requireNone));
    return true;
  }

  public ContentManager getContentManager() {
    return contentManager;
  }

  public boolean hasPermission(Identity identity, String path, Permission requestedPermission) {
    if (denyall)
      return false;

    if (identity == null)
      return false;

    Set<String> memberOf = identity.getMemberOf();
    boolean rulesSayYes = false;

    for (AccessRule rule : accessRules) {
      if (rule.applies(path)) {
        rulesSayYes = rule.isAccessAllowed(memberOf);
        break;
      }
    }
    if (rulesSayYes)
      return true;

    if (!identity.getEffectivePermissions().contains(requestedPermission))
      return false;

    if (accessRules.isEmpty())
      return true;

    return false;
  }
}
