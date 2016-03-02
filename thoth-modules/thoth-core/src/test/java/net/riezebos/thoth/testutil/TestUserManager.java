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

import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;

/**
 * @author wido
 */
public class TestUserManager implements UserManager {

  private Map<String, User> users = new HashMap<>();
  private Map<String, Group> groups = new HashMap<>();

  public TestUserManager() {
    Group administrators = new Group("administrators");
    administrators.addPermission(Permission.PULL);
    administrators.addPermission(Permission.REINDEX);
    registerGroup(administrators);

    Group writers = new Group("writers");
    writers.addPermission(Permission.BROWSE);
    writers.addPermission(Permission.DIFF);
    writers.addPermission(Permission.META);
    writers.addPermission(Permission.READ_FRAGMENTS);
    writers.addPermission(Permission.REVISION);
    writers.addPermission(Permission.VALIDATE);
    registerGroup(writers);

    Group readers = new Group("readers");
    readers.addPermission(Permission.ACCESS);
    readers.addPermission(Permission.READ_RESOURCE);
    readers.addPermission(Permission.READ_BOOKS);
    readers.addPermission(Permission.SEARCH);
    registerGroup(readers);

    User administrator = new User("administrator");
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
  public boolean deleteUser(User user) throws UserManagerException {
    return users.remove(user.getIdentifier()) != null;
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
  public boolean deleteGroup(Group group) throws UserManagerException {
    return groups.remove(group.getIdentifier()) != null;
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

}
