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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Identity implements Cloneable, Serializable, Comparable<Identity> {
  private static final long serialVersionUID = 1L;
  private Long id = null;
  private String identifier;
  private Set<Group> memberships = new HashSet<>();

  abstract public Set<Permission> getEffectivePermissions();

  @JsonIgnore
  abstract public String getTypeName();

  @JsonIgnore
  abstract public boolean isAdministrator();

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
    if (identifier != null)
      throw new IllegalArgumentException("Update of a persisted identity's identifier not allowed");
    identifier = name;
  }

  protected void registerMembership(Group group) {
    memberships.add(group);
  }

  protected void unregisterMembership(Group group) {
    memberships.remove(group);
  }

  @JsonIgnore
  public List<Group> getMemberships() {
    ArrayList<Group> groups = new ArrayList<>(memberships);
    Collections.sort(groups);
    return groups;
  }

  public Set<String> getMemberOf() {
    return memberships.stream().map(g -> g.getIdentifier()).collect(Collectors.toSet());
  }

  @Override
  public String toString() {
    return getIdentifier();
  }

  @JsonIgnore
  public String getDescription() {
    return getIdentifier();
  }

  @Override
  public int compareTo(Identity o) {
    if (o == null)
      return -1;
    return getIdentifier().compareTo(o.getIdentifier());
  }

}
