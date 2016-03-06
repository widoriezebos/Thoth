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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.SequenceGenerator;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;

public class GroupDao extends BaseDao {

  private ThothDB thothDB;

  /**
   * Package private constructor; intentionally: use IdentityDao in stead
   * 
   * @param thothDB
   */
  GroupDao(ThothDB thothDB) {
    this.thothDB = thothDB;
  }

  public void createGroup(Group group) throws UserManagerException {
    try (Connection connection = thothDB.getConnection();
        SqlStatement identityStmt = new SqlStatement(connection, thothDB.getQuery("insert_identity")); //
        SqlStatement groupStmt = new SqlStatement(connection, thothDB.getQuery("insert_group"))) {

      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, Tables.THOTH_IDENTITIES);
      long id = sequenceGenerator.getNextValue();

      identityStmt.setLong("id", id);
      identityStmt.setString("identifier", group.getIdentifier());
      identityStmt.executeUpdate();

      groupStmt.setLong("id", id);
      groupStmt.executeUpdate();
      commitReload(connection);
    } catch (SQLException | DatabaseException e) {
      throw new UserManagerException(e);
    }
  }

  /**
   * Updates the permissions as specified by the Group
   * 
   * @param group
   * @return
   * @throws UserManagerException
   */
  public boolean updatePermissions(Group group) throws UserManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement delPermissionStmt = new SqlStatement(connection, thothDB.getQuery("delete_permission"));
        SqlStatement insPermissionStmt = new SqlStatement(connection, thothDB.getQuery("insert_permission"))) {

      Set<Permission> current = getPermissions(connection, group);
      Set<Permission> toBeRemoved = new HashSet<>(current);
      toBeRemoved.removeAll(group.getPermissions());

      Set<Permission> toBeAdded = new HashSet<>(group.getPermissions());
      toBeAdded.removeAll(current);

      int count = 0;
      for (Permission remove : toBeRemoved) {
        delPermissionStmt.setLong("grou_id", group.getId());
        delPermissionStmt.setInt("permission", remove.getValue());
        count += delPermissionStmt.executeUpdate();
      }

      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, Tables.THOTH_PERMISSIONS);
      for (Permission permission : toBeAdded) {
        long id = sequenceGenerator.getNextValue();
        insPermissionStmt.setLong("id", id);
        insPermissionStmt.setLong("grou_id", group.getId());
        insPermissionStmt.setInt("permission", permission.getValue());
        count += insPermissionStmt.executeUpdate();
      }
      commitReload(connection);
      return count != 0;
    } catch (SQLException | DatabaseException e) {
      throw new UserManagerException(e);
    }
  }

  public boolean deleteGroup(Group group) throws UserManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement memberStmtIden = new SqlStatement(connection, thothDB.getQuery("delete_membership_iden")); //
        SqlStatement memberStmtGrou = new SqlStatement(connection, thothDB.getQuery("delete_membership_grou")); //
        SqlStatement deletePermissionsStmt = new SqlStatement(connection, thothDB.getQuery("delete_permissions"));
        SqlStatement groupStmt = new SqlStatement(connection, thothDB.getQuery("delete_group")); //
        SqlStatement identityStmt = new SqlStatement(connection, thothDB.getQuery("delete_identity"))) {

      memberStmtIden.setLong("iden_id", group.getId());
      memberStmtIden.executeUpdate();

      memberStmtGrou.setLong("grou_id", group.getId());
      memberStmtGrou.executeUpdate();

      deletePermissionsStmt.setLong("grou_id", group.getId());
      deletePermissionsStmt.executeUpdate();

      groupStmt.setLong("id", group.getId());
      groupStmt.executeUpdate();

      int count;
      identityStmt.setLong("id", group.getId());
      count = identityStmt.executeUpdate();
      commitReload(connection);
      return count == 1;
    } catch (SQLException e) {
      throw new UserManagerException(e);
    }
  }

  public void createMembership(Group group, Identity identity) throws UserManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement memberStmt = new SqlStatement(connection, thothDB.getQuery("insert_membership"))) {

      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, Tables.THOTH_MEMBERSHIPS);
      long id = sequenceGenerator.getNextValue();
      memberStmt.setLong("id", id);
      memberStmt.setLong("grou_id", group.getId());
      memberStmt.setLong("iden_id", identity.getId());
      memberStmt.executeUpdate();
      commitReload(connection);
    } catch (SQLException | DatabaseException e) {
      throw new UserManagerException(e);
    }
  }

  public void deleteMembership(Group group, Identity identity) throws UserManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement memberStmt = new SqlStatement(connection, thothDB.getQuery("delete_membership_grou_iden"))) {
      memberStmt.setLong("grou_id", group.getId());
      memberStmt.setLong("iden_id", identity.getId());
      memberStmt.executeUpdate();
      commitReload(connection);
    } catch (SQLException e) {
      throw new UserManagerException(e);
    }
  }

  protected List<Group> getGroups() throws SQLException {

    List<Group> result = new ArrayList<>();

    try (Connection connection = thothDB.getConnection(); //
        SqlStatement groupStmt = new SqlStatement(connection, thothDB.getQuery("select_groups")); //
        ResultSet rs = groupStmt.executeQuery()) {
      while (rs.next()) {
        long id = rs.getLong(1);
        String identifier = rs.getString(2);
        Group group = new Group(id, identifier);
        result.add(group);
        Set<Permission> permissions = getPermissions(connection, group);
        group.addPermissions(permissions);
      }
    }

    return result;
  }

  protected Set<Permission> getPermissions(Connection connection, Group group) throws SQLException {
    try (SqlStatement permissionStmt = new SqlStatement(connection, thothDB.getQuery("select_permissions"))) {
      permissionStmt.set("grou_id", group.getId());
      Set<Permission> result = new HashSet<>();
      try (ResultSet permRs = permissionStmt.executeQuery()) {
        while (permRs.next()) {
          int permission = permRs.getInt(1);
          result.add(Permission.convert(permission));
        }
      }
      return result;
    }
  }
}
