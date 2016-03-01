package net.riezebos.thoth.user.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.SequenceGenerator;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.Group;
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
    try (Connection connection = thothDB.getConnection()) {
      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, "thoth_identities");
      long id = sequenceGenerator.getNextValue();

      SqlStatement identityStmt = new SqlStatement(connection, "insert into thoth_identities(id, identifier) values (:id, :identifier)");
      identityStmt.setLong("id", id);
      identityStmt.setString("identifier", group.getIdentifier());
      identityStmt.executeUpdate();

      SqlStatement groupStmt = new SqlStatement(connection, "insert into thoth_groups(id)\n" + //
          "values (:id)");
      groupStmt.setLong("id", id);

      groupStmt.executeUpdate();
      connection.commit();
      reloadCaches();
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
  public boolean updateGroup(Group group) throws UserManagerException {
    try (Connection connection = thothDB.getConnection()) {
      SqlStatement delPermissionStmt = new SqlStatement(connection, "delete from thoth_permissions\n" + //
          "where grou_id = :id");
      delPermissionStmt.setLong("id", group.getId());
      int count = delPermissionStmt.executeUpdate();

      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, "thoth_permissions");
      for (Permission permission : group.getPermissions()) {
        long id = sequenceGenerator.getNextValue();
        SqlStatement insPermissionStmt =
            new SqlStatement(connection, "insert into from thoth_permissions(id, grou_id, permission) values(:id, :grou_id, :permission)");
        insPermissionStmt.setLong("id", id);
        insPermissionStmt.setLong("grou_id", group.getId());
        insPermissionStmt.setInt("permission", permission.getValue());
        count += insPermissionStmt.executeUpdate();
      }

      connection.commit();
      return count != 0;
    } catch (SQLException | DatabaseException e) {
      throw new UserManagerException(e);
    }
  }

  public boolean deleteGroup(Group group) throws UserManagerException {
    try (Connection connection = thothDB.getConnection()) {

      SqlStatement deletePermissionsStmt = new SqlStatement(connection, "delete from thoth_permissions\n" + //
          "where grou_id = :id");
      deletePermissionsStmt.setLong("id", group.getId());
      deletePermissionsStmt.executeUpdate();

      SqlStatement memberStmt = new SqlStatement(connection, "delete from thoth_memberships\n" + //
          "where iden_id = :id");
      memberStmt.setLong("id", group.getId());
      memberStmt.executeUpdate();

      memberStmt = new SqlStatement(connection, "delete from thoth_memberships\n" + //
          "where grou_id = :id");
      memberStmt.setLong("id", group.getId());
      memberStmt.executeUpdate();

      SqlStatement userStmt = new SqlStatement(connection, "delete from thoth_groups\n" + //
          "where id = :id");
      userStmt.setLong("id", group.getId());
      userStmt.executeUpdate();

      SqlStatement identityStmt = new SqlStatement(connection, "delete from thoth_identities\n" + //
          "where id = :id");
      identityStmt.setLong("id", group.getId());
      int count = identityStmt.executeUpdate();
      connection.commit();
      reloadCaches();
      return count == 1;
    } catch (SQLException e) {
      throw new UserManagerException(e);
    }
  }

  protected List<Group> getGroups() throws SQLException {

    List<Group> result = new ArrayList<>();

    try (Connection connection = thothDB.getConnection()) {
      SqlStatement groupStmt = new SqlStatement(connection,
          "select iden.id, iden.identifier " + //
              "from thoth_identities as iden, thoth_groups as grou " + //
              "where iden.id = grou.id");
      try (ResultSet rs = groupStmt.executeQuery()) {
        while (rs.next()) {
          long id = rs.getLong(1);
          String identifier = rs.getString(2);
          Group group = new Group(id, identifier);
          result.add(group);
          loadPermissions(connection, group);
        }
      }
    }

    return result;
  }

  protected void loadPermissions(Connection connection, Group group) throws SQLException {
    SqlStatement permissionStmt = new SqlStatement(connection,
        "select perm.permission " + //
            "from thoth_permissions as perm " + //
            "where perm.grou_id = :groupId");
    permissionStmt.set("groupId", group.getId());
    try (ResultSet permRs = permissionStmt.executeQuery()) {
      while (permRs.next()) {
        int permission = permRs.getInt(1);
        group.addPermission(Permission.convert(permission));
      }
    }
  }
}
