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

public class Identity {
  private String identifier;
  private Set<Group> memberships = new HashSet<>();

  public Identity(String identifier) {
    this.identifier = identifier;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String name) {
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
}
