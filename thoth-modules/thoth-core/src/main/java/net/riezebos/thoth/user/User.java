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

public class User extends Identity {

  private String passwordhash;
  private String emailaddress;
  private String firstname;
  private String lastname;

  public User(String identifier) {
    super(identifier);
  }

  public User(long id, String identifier) {
    super(id, identifier);
  }

  public Set<Permission> getPermissions() {
    Set<Permission> result = new HashSet<>();
    for (Group group : getMemberships())
      result.addAll(group.getPermissions());
    return result;
  }

  public boolean isAllowed(Permission permission) {
    return getPermissions().contains(permission);
  }

  public void setPasswordhash(String passwordhash) {
    this.passwordhash = passwordhash;
  }

  public void setEmailaddress(String emailaddress) {
    this.emailaddress = emailaddress;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getPasswordhash() {
    return passwordhash;
  }

  public String getEmailaddress() {
    return emailaddress;
  }

  public String getFirstname() {
    return firstname;
  }

  public String getLastname() {
    return lastname;
  }

}
