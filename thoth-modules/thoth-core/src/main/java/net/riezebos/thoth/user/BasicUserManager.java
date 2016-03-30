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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.dao.IdentityDao;
import net.riezebos.thoth.util.ExpiringCache;

/**
 * @author wido
 */
public class BasicUserManager implements UserManager {
  private static final int THIRTY_SECONDS = 30 * 1000;
  private static final Logger LOG = LoggerFactory.getLogger(BasicUserManager.class);

  private ExpiringCache<String, String> ssoTokenCache = new ExpiringCache<>(THIRTY_SECONDS);

  private IdentityDao identityDao;

  public BasicUserManager(ThothEnvironment thothEnvironment) throws UserManagerException {
    try {
      identityDao = new IdentityDao(thothEnvironment.getThothDB());
    } catch (DatabaseException e) {
      throw new UserManagerException(e);
    }
  }

  @Override
  public Identity getIdentity(String identifier) throws UserManagerException {
    if (StringUtils.isBlank(identifier))
      return null;

    return identityDao.getIdentities().get(identifier);
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
    List<User> users = filter.collect(Collectors.toList());
    Collections.sort(users);
    return users;
  }

  @Override
  public List<Group> listGroups() throws UserManagerException {
    Collection<Identity> identities = identityDao.getIdentities().values();
    Stream<Group> filter = identities.stream()//
        .filter(grp -> grp instanceof Group)//
        .map(g -> (Group) g);
    List<Group> groups = filter.collect(Collectors.toList());
    Collections.sort(groups);
    return groups;
  }

  @Override
  public User createUser(User user) throws UserManagerException {
    return identityDao.createUser(user);
  }

  @Override
  public boolean deleteIdentity(Identity identity) throws UserManagerException {
    if (identity == null)
      return false;
    if (identity instanceof User)
      return identityDao.deleteUser((User) identity);
    if (identity instanceof Group)
      return identityDao.deleteGroup((Group) identity);
    throw new IllegalArgumentException("Unsupported identity type: " + identity.getTypeName());
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
  public <T extends Identity> T merge(T identity) throws UserManagerException {
    return identityDao.merge(identity);
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
