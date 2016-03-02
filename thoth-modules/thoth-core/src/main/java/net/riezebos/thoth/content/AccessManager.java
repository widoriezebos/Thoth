package net.riezebos.thoth.content;

import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;

public class AccessManager {

  private ContentManager contentManager;

  public AccessManager(ContentManager contentManager) {
    this.contentManager = contentManager;
  }

  public ContentManager getContentManager() {
    return contentManager;
  }

  public boolean hasPermission(Identity identity, String path, Permission requestedPermission) {
    if (identity == null)
      return false;

    return identity.getEffectivePermissions().contains(requestedPermission);
  }
}
