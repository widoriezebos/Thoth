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

public class Group extends Identity {

  private Set<Identity> members = new HashSet<>();
  private Set<Permission> permissions = new HashSet<>();

  public Group(String identifier) {
    super(identifier);
  }

  public Group(long id, String identifier) {
    super(id, identifier);
  }

  public Set<Identity> getMembers() {
    return members;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }

  public void addPermission(Permission permission) {
    permissions.add(permission);
  }

  public void removePermission(Permission permission) {
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

}
