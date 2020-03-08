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
package net.riezebos.thoth.user;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User extends Identity implements Cloneable {
  private static final long serialVersionUID = 1L;
  private static final String ADMINISTRATOR = "administrator";
  @JsonIgnore
  private String passwordhash;
  private String emailaddress;
  private String firstname;
  private String lastname;
  private Date blockedUntil;
  private Set<Permission> cachedEffectivePermissions = null;

  public User(String identifier) {
    super(identifier);
  }

  public User(long id, String identifier) {
    super(id, identifier);
  }

  @Override
  public User clone() {
    User result = (User) super.clone();
    return result;
  }

  public Set<Permission> getPermissions() {
    if (cachedEffectivePermissions == null) {
      Set<Permission> permissions = new HashSet<>();
      if (isAdministrator())
        permissions.addAll(Arrays.asList(Permission.values()));
      else
        for (Group group : getMemberships())
          permissions.addAll(group.getPermissions());
      cachedEffectivePermissions = permissions;
    }
    return cachedEffectivePermissions;
  }

  public boolean isAllowed(Permission permission) {
    return getPermissions().contains(permission);
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

  @JsonIgnore
  public String getPasswordhash() {
    return passwordhash;
  }

  @JsonIgnore
  public void setPasswordhash(String passwordhash) {
    this.passwordhash = passwordhash;
  }

  @JsonIgnore
  public void setPassword(String clearTextPassword) {
    PasswordUtil util = new PasswordUtil();
    passwordhash = util.hashPassword(clearTextPassword);
  }

  public boolean isValidPassword(String clearTextPassword) {
    PasswordUtil util = new PasswordUtil();
    return util.isValidPassword(clearTextPassword, getPasswordhash());
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

  public Date getBlockedUntil() {
    return blockedUntil;
  }

  public void setBlockedUntil(Date blockedUntil) {
    this.blockedUntil = blockedUntil;
  }

  @Override
  @JsonIgnore
  public Set<Permission> getEffectivePermissions() {
    return getPermissions();
  }

  @JsonIgnore
  @Override
  public String getDescription() {
    String result = StringUtils.isBlank(getFirstname()) ? "" : getFirstname() + " ";
    result += StringUtils.isBlank(getLastname()) ? "" : getLastname();
    result = result.trim();
    if (StringUtils.isBlank(result))
      result = getIdentifier();
    return result;
  }

  @JsonIgnore
  @Override
  public String getTypeName() {
    return "user";
  }

  @JsonIgnore
  @Override
  public boolean isAdministrator() {
    return ADMINISTRATOR.equals(getIdentifier());
  }
}
