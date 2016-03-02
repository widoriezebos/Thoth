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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.dao.IdentityDao;

/**
 * @author wido
 */
public class BasicUserManager implements UserManager {

  IdentityDao identityDao;

  public BasicUserManager(ThothEnvironment thothEnvironment) throws UserManagerException {
    try {
      identityDao = new IdentityDao(thothEnvironment.getThothDB());
    } catch (DatabaseException e) {
      throw new UserManagerException(e);
    }
  }

  @Override
  public User getUser(String identifier) throws UserManagerException {
    if (StringUtils.isBlank(identifier))
      return null;

    Identity identity = identityDao.getIdentities().get(identifier);
    if (identity instanceof User)
      return (User) identity;
    return null;
  }

  @Override
  public Group getGroup(String identifier) throws UserManagerException {
    if (StringUtils.isBlank(identifier))
      return null;

    Identity identity = identityDao.getIdentities().get(identifier);
    if (identity instanceof Group)
      return (Group) identity;
    return null;
  }

  @Override
  public List<User> listUsers() throws UserManagerException {
    Collection<Identity> identities = identityDao.getIdentities().values();
    Stream<User> filter = identities.stream()//
        .filter(usr -> usr instanceof User)//
        .map(u -> (User) u);
    return filter.collect(Collectors.toList());
  }

  @Override
  public List<Group> listGroups() throws UserManagerException {
    Collection<Identity> identities = identityDao.getIdentities().values();
    Stream<Group> filter = identities.stream()//
        .filter(grp -> grp instanceof Group)//
        .map(g -> (Group) g);
    return filter.collect(Collectors.toList());
  }

  @Override
  public User createUser(User user) throws UserManagerException {
    return identityDao.createUser(user);
  }

  @Override
  public boolean deleteUser(User user) throws UserManagerException {
    return identityDao.deleteUser(user);
  }

  @Override
  public boolean updateUser(User user) throws UserManagerException {
    return identityDao.updateUser(user);
  }

  @Override
  public Group createGroup(Group group) throws UserManagerException {
    identityDao.createGroup(group);
    return merge(group);
  }

  @Override
  public boolean deleteGroup(Group group) throws UserManagerException {
    return identityDao.deleteGroup(group);
  }

  @Override
  public boolean updatePermissions(Group group) throws UserManagerException {
    return identityDao.updatePermissions(group);
  }

  @Override
  public void createMembership(Group group, Identity identity) throws UserManagerException {
    identityDao.createMembership(group, identity);
  }

  @Override
  public void deleteMembership(Group group, Identity identity) throws UserManagerException {
    identityDao.deleteMembership(group, identity);
  }

  @Override
  public <T extends Identity> T merge(T identity) {
    return identityDao.merge(identity);
  }

}
