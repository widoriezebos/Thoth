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
import net.riezebos.thoth.user.User;

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
    try (Connection connection = thothDB.getConnection()) {
      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, "thoth_identities");
      long id = sequenceGenerator.getNextValue();

      SqlStatement identityStmt = new SqlStatement(connection, "insert into thoth_identities(id, identifier) values (:id, :identifier)");
      identityStmt.setLong("id", id);
      identityStmt.setString("identifier", user.getIdentifier());
      identityStmt.executeUpdate();

      SqlStatement userStmt = new SqlStatement(connection, "insert into thoth_users(id, passwordhash, emailaddress, firstname, lastname)\n" + //
          "values (:id, :passwordhash, :emailaddress, :firstname, :lastname)");
      userStmt.setLong("id", id);
      userStmt.setString("passwordhash", user.getPasswordhash());
      userStmt.setString("emailaddress", user.getEmailaddress());
      userStmt.setString("firstname", user.getFirstname());
      userStmt.setString("lastname", user.getLastname());

      userStmt.executeUpdate();
      connection.commit();
      reloadCaches();
    } catch (SQLException | DatabaseException e) {
      throw new UserManagerException(e);
    }
  }

  public boolean updateUser(User user) throws UserManagerException {
    try (Connection connection = thothDB.getConnection()) {
      SqlStatement userStmt = new SqlStatement(connection,
          "update thoth_users set passwordhash = :passwordhash, emailaddress = :emailaddress, firstname = :firstname, lastname = :lastname\n" + //
              "where id = :id");
      userStmt.setLong("id", user.getId());
      userStmt.setString("passwordhash", user.getPasswordhash());
      userStmt.setString("emailaddress", user.getEmailaddress());
      userStmt.setString("firstname", user.getFirstname());
      userStmt.setString("lastname", user.getLastname());

      int count = userStmt.executeUpdate();
      connection.commit();
      return count == 1;
    } catch (SQLException e) {
      throw new UserManagerException(e);
    }
  }

  public boolean deleteUser(User user) throws UserManagerException {
    try (Connection connection = thothDB.getConnection()) {
      SqlStatement userStmt = new SqlStatement(connection, "delete from thoth_users\n" + //
          "where id = :id");
      userStmt.setLong("id", user.getId());

      int count = userStmt.executeUpdate();
      connection.commit();
      reloadCaches();
      return count == 1;
    } catch (SQLException e) {
      throw new UserManagerException(e);
    }
  }

  public List<User> getUsers() throws SQLException {

    List<User> result = new ArrayList<>();

    try (Connection connection = thothDB.getConnection()) {
      SqlStatement stmt = new SqlStatement(connection,
          "select iden.id, iden.identifier, usr.passwordhash, usr.emailaddress, usr.firstname, usr.lastname\n" + //
              "from thoth_identities as iden, thoth_users as usr\n" + //
              "where iden.id = usr.id");
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int idx = 1;
          long id = rs.getLong(idx++);
          String identifier = rs.getString(idx++);
          String passwordhash = rs.getString(idx++);
          String emailaddress = rs.getString(idx++);
          String firstname = rs.getString(idx++);
          String lastname = rs.getString(idx++);
          User user = new User(id, identifier);
          user.setPasswordhash(passwordhash);
          user.setEmailaddress(emailaddress);
          user.setFirstname(firstname);
          user.setLastname(lastname);

          result.add(user);
        }
      }
    }
    return result;
  }
}
