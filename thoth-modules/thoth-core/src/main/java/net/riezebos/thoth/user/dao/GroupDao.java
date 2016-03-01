package net.riezebos.thoth.user.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
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
