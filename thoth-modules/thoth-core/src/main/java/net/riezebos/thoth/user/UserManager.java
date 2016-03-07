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

import java.util.List;

import net.riezebos.thoth.exceptions.UserManagerException;

public interface UserManager {

  public static final String SSO_TOKEN_NAME = "ssotoken";

  User getUser(String identifier) throws UserManagerException;

  Group getGroup(String identifier) throws UserManagerException;

  List<User> listUsers() throws UserManagerException;

  List<Group> listGroups() throws UserManagerException;

  User createUser(User user) throws UserManagerException;

  boolean deleteIdentity(Identity identity) throws UserManagerException;

  boolean updateUser(User user) throws UserManagerException;

  Group createGroup(Group group) throws UserManagerException;

  boolean updatePermissions(Group group) throws UserManagerException;

  void createMembership(Group group, Identity identity) throws UserManagerException;

  void deleteMembership(Group group, Identity identity) throws UserManagerException;

  <T extends Identity> T merge(T identity) throws UserManagerException;

  Identity getIdentity(String identifier) throws UserManagerException;

  String generateSSOToken(Identity identity);

  Identity getIdentityForToken(String token);

}
