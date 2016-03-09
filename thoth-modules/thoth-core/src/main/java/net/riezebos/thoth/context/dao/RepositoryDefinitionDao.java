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
package net.riezebos.thoth.context.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.SequenceGenerator;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.context.RepositoryType;
import net.riezebos.thoth.exceptions.ContextManagerException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.user.PasswordUtil;
import net.riezebos.thoth.util.BaseDao;

public class RepositoryDefinitionDao extends BaseDao {

  private ThothDB thothDB;

  /**
   * Package private constructor; intentionally: use ContextDefinitionDao in stead
   * 
   * @param thothDB
   */
  RepositoryDefinitionDao(ThothDB thothDB) {
    this.thothDB = thothDB;
  }

  public RepositoryDefinition createRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement repoStmt = new SqlStatement(connection, thothDB.getQuery("insert_repository"))) {

      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, Tables.THOTH_REPOSITORIES);
      long id = sequenceGenerator.getNextValue();

      repoStmt.setLong("id", id);
      repoStmt.setString("name", repositoryDefinition.getName());
      repoStmt.setString("repotype", repositoryDefinition.getType().getValue());
      repoStmt.setString("location", repositoryDefinition.getLocation());
      repoStmt.setString("username", repositoryDefinition.getUsername());
      repoStmt.setString("password", encrypt(repositoryDefinition.getPassword()));
      repoStmt.executeUpdate();
      repositoryDefinition.setId(id);
      commitReload(connection);
      return repositoryDefinition;
    } catch (SQLException | DatabaseException e) {
      throw new ContextManagerException(e);
    }
  }

  public boolean updateRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement repoStmt = new SqlStatement(connection, thothDB.getQuery("update_repository"))) {

      repoStmt.setLong("id", repositoryDefinition.getId());
      repoStmt.setString("name", repositoryDefinition.getName());
      repoStmt.setString("repotype", repositoryDefinition.getType().getValue());
      repoStmt.setString("location", repositoryDefinition.getLocation());
      repoStmt.setString("username", repositoryDefinition.getUsername());
      repoStmt.setString("password", encrypt(repositoryDefinition.getPassword()));
      int count = repoStmt.executeUpdate();

      commitReload(connection);
      return count == 1;
    } catch (SQLException e) {
      throw new ContextManagerException(e);
    }
  }

  public boolean isInUse(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement repoStmt = new SqlStatement(connection, thothDB.getQuery("select_is_used"))) {

      repoStmt.setLong("id", repositoryDefinition.getId());
      try (ResultSet rs = repoStmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          return count != 0;
        } else
          return false;
      }
    } catch (SQLException e) {
      throw new ContextManagerException(e);
    }

  }

  public boolean deleteRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    if (isInUse(repositoryDefinition))
      throw new ContextManagerException("Repository " + repositoryDefinition + " is in use by one or more Contexts. Cannot delete");
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement repoStmt = new SqlStatement(connection, thothDB.getQuery("delete_repository"))) {

      repoStmt.setLong("id", repositoryDefinition.getId());
      int count = repoStmt.executeUpdate();
      commitReload(connection);
      return count == 1;
    } catch (SQLException e) {
      throw new ContextManagerException(e);
    }

  }

  public Map<String, RepositoryDefinition> getRepositoryDefinitions() throws SQLException {
    Map<String, RepositoryDefinition> result = new HashMap<>();

    try (Connection connection = thothDB.getConnection(); //
        SqlStatement stmt = new SqlStatement(connection, thothDB.getQuery("select_repositories")); //
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        int idx = 1;
        long id = rs.getLong(idx++);
        String name = rs.getString(idx++);
        String repotype = rs.getString(idx++);
        String location = rs.getString(idx++);
        String username = rs.getString(idx++);
        String password = rs.getString(idx++);

        RepositoryDefinition repositoryDefinition = new RepositoryDefinition();
        repositoryDefinition.setId(id);
        repositoryDefinition.setImmutable(false);
        repositoryDefinition.setName(name);
        repositoryDefinition.setType(RepositoryType.convert(repotype));
        repositoryDefinition.setLocation(location);
        repositoryDefinition.setUsername(username);
        repositoryDefinition.setPassword(decrypt(password));

        result.put(repositoryDefinition.getName().toLowerCase(), repositoryDefinition);
      }
    }
    return result;
  }

  protected String encrypt(String clearTextPassword) {
    Configuration configuration = thothDB.getConfiguration();
    String masterPassword = configuration.getPasswordEncryptionKey();
    PasswordUtil passwordUtil = new PasswordUtil();
    return passwordUtil.encrypt(masterPassword, clearTextPassword);
  }

  protected String decrypt(String encryptedPassword) {
    Configuration configuration = thothDB.getConfiguration();
    String masterPassword = configuration.getPasswordEncryptionKey();
    PasswordUtil passwordUtil = new PasswordUtil();
    return passwordUtil.decrypt(masterPassword, encryptedPassword);
  }

}
