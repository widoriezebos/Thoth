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
package net.riezebos.thoth.testutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;
import net.riezebos.thoth.util.ExpiringCache;

/**
 * @author wido
 */
public class TestUserManager implements UserManager {

  public static final String ADMINISTRATORPW = "administratorpw";
  private static final Logger LOG = LoggerFactory.getLogger(TestUserManager.class);
  private static final int THIRTY_SECONDS = 30 * 1000;
  private Map<String, User> users = new HashMap<>();
  private Map<String, Group> groups = new HashMap<>();
  private ExpiringCache<String, String> ssoTokenCache = new ExpiringCache<>(THIRTY_SECONDS);

  public TestUserManager() {
    Group administrators = new Group("administrators");
    administrators.grantPermission(Permission.PULL);
    administrators.grantPermission(Permission.REINDEX);
    registerGroup(administrators);

    Group writers = new Group("writers");
    writers.grantPermission(Permission.BROWSE);
    writers.grantPermission(Permission.DIFF);
    writers.grantPermission(Permission.META);
    writers.grantPermission(Permission.READ_FRAGMENTS);
    writers.grantPermission(Permission.REVISION);
    writers.grantPermission(Permission.VALIDATE);
    registerGroup(writers);

    Group readers = new Group("readers");
    readers.grantPermission(Permission.BASIC_ACCESS);
    readers.grantPermission(Permission.READ_RESOURCE);
    readers.grantPermission(Permission.READ_BOOKS);
    readers.grantPermission(Permission.SEARCH);
    registerGroup(readers);

    User administrator = new User("administrator");
    administrator.setPassword(ADMINISTRATORPW);
    administrators.addMember(administrator);

    User writer = new User("writer");
    writers.addMember(writer);
    writers.addMember(administrators);

    User reader = new User("reader");
    readers.addMember(reader);
    readers.addMember(writers);
    readers.addMember(administrators);

    registerUser(reader);
    registerUser(writer);
    registerUser(administrator);
  }

  @Override
  public User getUser(String identifier) {
    return users.get(identifier);
  }

  public void registerUser(User user) {
    users.put(user.getIdentifier(), user);
  }

  @Override
  public Group getGroup(String identifier) {
    return groups.get(identifier);
  }

  public void registerGroup(Group group) {
    groups.put(group.getIdentifier(), group);
  }

  @Override
  public List<User> listUsers() throws UserManagerException {
    return new ArrayList<>(users.values());
  }

  @Override
  public List<Group> listGroups() throws UserManagerException {
    return new ArrayList<>(groups.values());
  }

  @Override
  public User createUser(User user) throws UserManagerException {
    users.put(user.getIdentifier(), user);
    return user;
  }

  @Override
  public boolean updateUser(User user) throws UserManagerException {
    users.put(user.getIdentifier(), user);
    return true;
  }

  @Override
  public Group createGroup(Group group) throws UserManagerException {
    registerGroup(group);
    return group;
  }

  @Override
  public boolean deleteIdentity(Identity identity) throws UserManagerException {
    boolean doneOne = groups.remove(identity.getIdentifier()) != null;
    doneOne |= users.remove(identity.getIdentifier()) != null;
    return doneOne;
  }

  @Override
  public boolean updatePermissions(Group group) throws UserManagerException {
    return true;
  }

  @Override
  public void createMembership(Group group, Identity identity) throws UserManagerException {
    group.addMember(identity);
  }

  @Override
  public void deleteMembership(Group group, Identity identity) throws UserManagerException {
    group.removeMember(identity);
  }

  @Override
  public <T extends Identity> T merge(T identity) {
    return identity;
  }

  @Override
  public Identity getIdentity(String identifier) throws UserManagerException {
    User user = getUser(identifier);
    if (user != null)
      return user;
    return getGroup(identifier);
  }

  @Override
  public String generateSSOToken(Identity identity) {
    String token = UUID.randomUUID().toString();
    ssoTokenCache.put(token, identity.getIdentifier());
    return token;
  }

  @Override
  public Identity getIdentityForToken(String token) {
    try {
      return getIdentity(ssoTokenCache.get(token));
    } catch (UserManagerException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }

}
