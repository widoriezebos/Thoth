package net.riezebos.thoth.user.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.util.FinalWrapper;

public class IdentityDao implements CacheListener {
  private static final Logger LOG = LoggerFactory.getLogger(IdentityDao.class);

  private FinalWrapper<Map<String, Identity>> identitiesWrapper = null;
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
    FinalWrapper<Map<String, Identity>> wrapper = identitiesWrapper;
    if (wrapper == null) {
      synchronized (this) {
        if (identitiesWrapper == null) {
          identitiesWrapper = new FinalWrapper<>(doGetIdentities());
        }
        wrapper = identitiesWrapper;
      }
    }
    return wrapper.value;
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

      try (Connection connection = thothDB.getConnection()) {
        for (Group group : groups) {
          SqlStatement permissionStmt = new SqlStatement(connection,
              "select memb.iden_id " + //
                  "from thoth_memberships memb " + //
                  "where memb.grou_id = :groupId");
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

  public void reloadCaches() throws UserManagerException {
    identitiesWrapper = new FinalWrapper<>(doGetIdentities());
  }

  @Override
  public void invalidateCache() {
    try {
      reloadCaches();
    } catch (UserManagerException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  public void createUser(User user) throws UserManagerException {
    userDao.createUser(user);
  }

  public boolean updateUser(User user) throws UserManagerException {
    return userDao.updateUser(user);
  }

  public boolean deleteUser(User user) throws UserManagerException {
    return userDao.deleteUser(user);
  }

  public void createGroup(Group group) throws UserManagerException {
    groupDao.createGroup(group);
  }

  public boolean deleteGroup(Group group) throws UserManagerException {
    return groupDao.deleteGroup(group);
  }

}
