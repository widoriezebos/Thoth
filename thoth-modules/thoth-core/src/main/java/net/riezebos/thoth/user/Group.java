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
package net.riezebos.thoth.user;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Group extends Identity implements Cloneable {
  private static final long serialVersionUID = 1L;
  private Set<Permission> permissions = new HashSet<>();
  @JsonIgnore
  private Set<Identity> members = new HashSet<>();
  @JsonIgnore
  private Set<Permission> cachedEffectivePermissions = null;

  public Group(String identifier) {
    super(identifier);
  }

  public Group(long id, String identifier) {
    super(id, identifier);
  }

  @Override
  public Group clone() {
    Group result = (Group) super.clone();
    result.members = new HashSet<>(members);
    result.permissions = new HashSet<>(permissions);
    return result;
  }

  public Set<Identity> getMembers() {
    return members;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }

  public void grantPermission(Permission permission) {
    permissions.add(permission);
  }

  public void revokePermission(Permission permission) {
    permissions.remove(permission);
  }

  public void addMember(Identity identity) {
    members.add(identity);
    identity.registerMembership(this);
  }

  public void removeMember(Identity identity) {
    members.remove(identity);
    identity.unregisterMembership(this);
  }

  public void clearPermissions() {
    permissions.clear();
  }

  public void addPermissions(Set<Permission> permissions) {
    this.permissions.addAll(permissions);
  }

  @JsonIgnore
  @Override
  public Set<Permission> getEffectivePermissions() {
    if (cachedEffectivePermissions == null) {
      Set<Permission> permissions = new HashSet<>(getPermissions());
      for (Group group : getMemberships())
        permissions.addAll(group.getEffectivePermissions());
      cachedEffectivePermissions = permissions;
    }
    return cachedEffectivePermissions;
  }

  @JsonIgnore
  @Override
  public String getTypeName() {
    return "group";
  }

  @Override
  public boolean isAdministrator() {
    return false;
  }

}
