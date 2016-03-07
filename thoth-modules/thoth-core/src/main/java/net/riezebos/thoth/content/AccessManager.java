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

  private Pattern requirePattern = Pattern.compile("(.*?)\\s+require\\s+(.*)");
  private ContentManager contentManager;
  private List<AccessRule> accessRules = new ArrayList<>();
  private boolean denyall = false;

  public AccessManager(ContentManager contentManager) {
    this.contentManager = contentManager;
    FileHandle accessRulesFile = contentManager.getFileHandle(ThothUtil.suffix(contentManager.getLibraryRoot(), "/") + ACCESS_RULES_FILE);
    if (accessRulesFile.exists()) {
      loadAccessRules(accessRulesFile);
    } else {
      LOG.info("No access rules file found at " + accessRulesFile.getAbsolutePath() + " so only defaults apply");
    }
  }

  protected void loadAccessRules(FileHandle accessRulesFile) {
    try {
      String rules = ThothUtil.readInputStream(accessRulesFile.getInputStream());
      for (String rule : rules.split("\n")) {
        if (StringUtils.isNotBlank(rule) && !rule.trim().startsWith("#")) {
          Matcher matcher = requirePattern.matcher(rule);
          if (!matcher.matches()) {
            LOG.warn("Invalid rule, require keyword is missing in line: " + rule);
          } else {
            String path = matcher.group(1).trim();
            String groups = matcher.group(2).trim();
            accessRules.add(new AccessRule(path, groups));
          }
        }

      }
    } catch (Exception e) {
      denyall = true;
      LOG.error("As a consequence all access to " + contentManager.getContextName() + " will be DENIED");
    }
  }

  public ContentManager getContentManager() {
    return contentManager;
  }

  public boolean hasPermission(Identity identity, String path, Permission requestedPermission) {
    if (denyall)
      return false;

    if (identity == null)
      return false;

    if (!identity.getEffectivePermissions().contains(requestedPermission))
      return false;

    if (accessRules.isEmpty())
      return true;

    Set<String> memberOf = identity.getMemberOf();
    boolean result = false;

    for (AccessRule rule : accessRules) {
      if (rule.applies(path)) {
        result = rule.isAccessAllowed(memberOf);
        break;
      }
    }
    return result;
  }
}
