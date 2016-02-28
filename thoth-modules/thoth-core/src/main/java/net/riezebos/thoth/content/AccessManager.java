package net.riezebos.thoth.content;

import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;

public class AccessManager {

  private ContentManager contentManager;

  public AccessManager(ContentManager contentManager) {
    this.contentManager = contentManager;
  }

  public ContentManager getContentManager() {
    return contentManager;
  }

  public boolean hasPermission(User user, String path, Permission requestedPermission) {
    if (user == null)
      return false;

    return user.getPermissions().contains(requestedPermission);
  }
}
