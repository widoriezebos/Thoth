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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class Identity implements Cloneable, Serializable {
  private static final long serialVersionUID = 1L;
  private Long id = null;
  private String identifier;
  private Set<Group> memberships = new HashSet<>();

  abstract public Set<Permission> getEffectivePermissions();

  public Identity(String identifier) {
    this.identifier = identifier;
  }

  public Identity(long id, String identifier) {
    this.id = id;
    this.identifier = identifier;
  }

  @Override
  public Identity clone() {
    try {
      Identity result = (Identity) super.clone();
      result.memberships = new HashSet<>(memberships);
      return result;
    } catch (CloneNotSupportedException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(long id) {
    if (this.id != null)
      throw new IllegalArgumentException("Update of a persisted identity's id not allowed");
    this.id = id;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String name) {
    if (this.identifier != null)
      throw new IllegalArgumentException("Update of a persisted identity's identifier not allowed");
    this.identifier = name;
  }

  protected void registerMembership(Group group) {
    memberships.add(group);
  }

  protected void unregisterMembership(Group group) {
    memberships.remove(group);
  }

  public Set<Group> getMemberships() {
    return memberships;
  }

  @Override
  public String toString() {
    return getIdentifier();
  }

}
