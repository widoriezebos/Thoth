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
package net.riezebos.thoth.user.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.util.BaseDao;
import net.riezebos.thoth.util.CacheListener;

public class IdentityDao extends BaseDao implements CacheListener {

  private Map<String, Identity> identities = null;
  private ThothDB thothDB;
  private UserDao userDao;
  private GroupDao groupDao;

  public IdentityDao(ThothDB thothDB) {
    this.thothDB = thothDB;

    this.userDao = new UserDao(thothDB);
    this.userDao.registerCacheListener(this);

    this.groupDao = new GroupDao(thothDB);
    this.groupDao.registerCacheListener(this);
  }

  public Map<String, Identity> getIdentities() throws UserManagerException {
    Map<String, Identity> result = identities;
    if (result == null) {
      result = doGetIdentities();
      identities = result;
    }
    return result;
  }

  protected Map<String, Identity> doGetIdentities() throws UserManagerException {
    try {
      List<Identity> allIdentities = new ArrayList<>();
      List<Group> groups = groupDao.getGroups();
      allIdentities.addAll(userDao.getUsers());
      allIdentities.addAll(groups);

      Map<Long, Identity> identityMap = new HashMap<>();
      for (Identity identity : allIdentities)
        identityMap.put(identity.getId(), identity);

      try (Connection connection = thothDB.getConnection();
          SqlStatement permissionStmt = new SqlStatement(connection,
              "select memb.iden_id " + //
                  "from thoth_memberships memb " + //
                  "where memb.grou_id = :groupId")) {
        for (Group group : groups) {
          permissionStmt.set("groupId", group.getId());
          try (ResultSet permRs = permissionStmt.executeQuery()) {
            while (permRs.next()) {
              long idenId = permRs.getLong(1);
              Identity identity = identityMap.get(idenId);
              if (identity != null)
                group.addMember(identity);
            }
          }
        }
      }

      Map<String, Identity> result = new HashMap<>();
      for (Identity identity : allIdentities)
        result.put(identity.getIdentifier(), identity);
      return result;
    } catch (SQLException e) {
      throw new UserManagerException(e);
    }
  }

  @Override
  public void invalidateCache() {
    identities = null;
  }

  public User createUser(User user) throws UserManagerException {
    userDao.createUser(user);
    return merge(user);
  }

  public boolean updateUser(User user) throws UserManagerException {
    return userDao.updateUser(user);
  }

  public boolean deleteUser(User user) throws UserManagerException {
    return userDao.deleteUser(user);
  }

  public Group createGroup(Group group) throws UserManagerException {
    groupDao.createGroup(group);
    return merge(group);
  }

  public boolean deleteGroup(Group group) throws UserManagerException {
    return groupDao.deleteGroup(group);
  }

  public boolean updatePermissions(Group group) throws UserManagerException {
    return groupDao.updatePermissions(group);
  }

  public void createMembership(Group group, Identity identity) throws UserManagerException {
    groupDao.createMembership(group, identity);
  }

  public void deleteMembership(Group group, Identity identity) throws UserManagerException {
    groupDao.deleteMembership(group, identity);
  }

  @SuppressWarnings("unchecked")
  public <T extends Identity> T merge(T identity) throws UserManagerException {
    return (T) getIdentities().get(identity.getIdentifier());
  }

}
