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

import java.util.HashMap;
import java.util.Map;

/**
 * This will be replaced by proper user management in a future release. For now everything is hardcoded.
 * 
 * @author wido
 */
public class BasicUserManager implements UserManager {

  private Map<String, User> users = new HashMap<>();
  private Map<String, Group> groups = new HashMap<>();

  public BasicUserManager() {
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
    writers.addMember(administrator);

    User reader = new User("reader");
    readers.addMember(reader);
    readers.addMember(writer);
    readers.addMember(administrator);

    registerUser(reader);
    registerUser(writer);
    registerUser(administrator);
  }

  @Override
  public User getUser(String identifier) {
    return users.get(identifier);
  }

  @Override
  public void registerUser(User user) {
    users.put(user.getIdentifier(), user);
  }

  @Override
  public Group getGroup(String identifier) {
    return groups.get(identifier);
  }

  @Override
  public void registerGroup(Group group) {
    groups.put(group.getIdentifier(), group);
  }

}
