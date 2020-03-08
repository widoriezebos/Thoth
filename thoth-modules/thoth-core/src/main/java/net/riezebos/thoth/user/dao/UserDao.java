/* Copyright (c) 2020 W.T.J. Riezebos
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
import java.util.Date;
import java.util.List;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.SequenceGenerator;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.util.BaseDao;

public class UserDao extends BaseDao {

  private ThothDB thothDB;

  /**
   * Package private constructor; intentionally: use IdentityDao in stead
   *
   * @param thothDB
   */
  UserDao(ThothDB thothDB) {
    this.thothDB = thothDB;
  }

  public void createUser(User user) throws UserManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement identityStmt = new SqlStatement(connection, thothDB.getQuery("insert_identity")); //
        SqlStatement userStmt = new SqlStatement(connection, thothDB.getQuery("insert_user"))) {

      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, Tables.THOTH_IDENTITIES);
      long id = sequenceGenerator.getNextValue();

      identityStmt.setLong("id", id);
      identityStmt.setString("identifier", user.getIdentifier());
      identityStmt.executeUpdate();

      userStmt.setLong("id", id);
      userStmt.setString("passwordhash", user.getPasswordhash());
      userStmt.setString("emailaddress", user.getEmailaddress());
      userStmt.setString("firstname", user.getFirstname());
      userStmt.setString("lastname", user.getLastname());
      userStmt.setTimestamp("blockeduntil", user.getBlockedUntil());

      userStmt.executeUpdate();
      commitReload(connection);
    } catch (SQLException | DatabaseException e) {
      throw new UserManagerException(e);
    }
  }

  public boolean updateUser(User user) throws UserManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement userStmt = new SqlStatement(connection, thothDB.getQuery("update_user"))) {

      userStmt.setLong("id", user.getId());
      userStmt.setString("passwordhash", user.getPasswordhash());
      userStmt.setString("emailaddress", user.getEmailaddress());
      userStmt.setString("firstname", user.getFirstname());
      userStmt.setString("lastname", user.getLastname());
      userStmt.setTimestamp("blockeduntil", user.getBlockedUntil());

      int count = userStmt.executeUpdate();
      commitOnly(connection);
      return count == 1;
    } catch (SQLException e) {
      throw new UserManagerException(e);
    }
  }

  public boolean deleteUser(User user) throws UserManagerException {
    if (user.isAdministrator())
      throw new UserManagerException("Cannot delete the administrator user");

    try (Connection connection = thothDB.getConnection(); //
        SqlStatement identityStmt = new SqlStatement(connection, thothDB.getQuery("delete_identity")); //
        SqlStatement userStmt = new SqlStatement(connection, thothDB.getQuery("delete_user")); //
        SqlStatement memberStmt = new SqlStatement(connection, thothDB.getQuery("delete_membership_iden"))) {

      memberStmt.setLong("iden_id", user.getId());
      memberStmt.executeUpdate();

      userStmt.setLong("id", user.getId());
      userStmt.executeUpdate();

      identityStmt.setLong("id", user.getId());
      int count = identityStmt.executeUpdate();
      commitReload(connection);
      return count == 1;
    } catch (SQLException e) {
      throw new UserManagerException(e);
    }

  }

  public List<User> getUsers() throws SQLException {

    List<User> result = new ArrayList<>();

    try (Connection connection = thothDB.getConnection(); //
        SqlStatement stmt = new SqlStatement(connection, thothDB.getQuery("select_users")); //
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        int idx = 1;
        long id = rs.getLong(idx++);
        String identifier = rs.getString(idx++);
        String passwordhash = rs.getString(idx++);
        String emailaddress = rs.getString(idx++);
        String firstname = rs.getString(idx++);
        String lastname = rs.getString(idx++);
        Date blockedUntil = rs.getTimestamp(idx++);
        User user = new User(id, identifier);
        user.setPasswordhash(passwordhash);
        user.setEmailaddress(emailaddress);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setBlockedUntil(blockedUntil);

        result.add(user);
      }
    }
    return result;
  }
}
