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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.SequenceGenerator;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.exceptions.ContextManagerException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.util.BaseDao;
import net.riezebos.thoth.util.CacheListener;

public class ContextDefinitionDao extends BaseDao implements CacheListener {
  private static final Logger LOG = LoggerFactory.getLogger(ContextDefinitionDao.class);

  private ThothDB thothDB;
  private RepositoryDefinitionDao repoDao;
  private Map<String, ContextDefinition> contextDefinitions = null;
  private Map<String, RepositoryDefinition> repositoryDefinitions = null;

  public ContextDefinitionDao(ThothDB thothDB) {
    this.thothDB = thothDB;
    this.repoDao = new RepositoryDefinitionDao(thothDB);
    this.registerCacheListener(this);
    repoDao.registerCacheListener(this);
  }

  @Override
  public void invalidateCache() {
    contextDefinitions = null;
    repositoryDefinitions = null;
  }

  public ContextDefinition createContextDefinition(ContextDefinition contextDefinition) throws ContextManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement contextStmt = new SqlStatement(connection, thothDB.getQuery("insert_context"))) {

      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, Tables.THOTH_CONTEXTS);
      long id = sequenceGenerator.getNextValue();

      contextStmt.setLong("id", id);
      contextStmt.setString("name", contextDefinition.getName());
      contextStmt.setLong("repo_id", contextDefinition.getRepositoryDefinition().getId());
      contextStmt.setString("branch", contextDefinition.getBranch());
      contextStmt.setString("library", contextDefinition.getLibraryRoot());
      contextStmt.setInt("refreshinterval", (int) (contextDefinition.getRefreshInterval()));
      contextStmt.executeUpdate();
      contextDefinition.setId(id);
      commitReload(connection);
      return contextDefinition;
    } catch (SQLException | DatabaseException e) {
      throw new ContextManagerException(e);
    }
  }

  public boolean updateContextDefinition(ContextDefinition contextDefinition) throws ContextManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement contextStmt = new SqlStatement(connection, thothDB.getQuery("update_context"))) {

      Long id = contextDefinition.getId();
      if (id == null)
        throw new IllegalArgumentException("ContextDefinition was not persisted and merged yet. Cannot update");
      contextStmt.setLong("id", id);
      contextStmt.setString("name", contextDefinition.getName());
      contextStmt.setLong("repo_id", contextDefinition.getRepositoryDefinition().getId());
      contextStmt.setString("branch", contextDefinition.getBranch());
      contextStmt.setString("library", contextDefinition.getLibraryRoot());
      contextStmt.setInt("refreshinterval", (int) (contextDefinition.getRefreshInterval()));
      contextStmt.executeUpdate();

      int count = contextStmt.executeUpdate();
      commitReload(connection);
      return count == 1;
    } catch (SQLException e) {
      throw new ContextManagerException(e);
    }
  }

  public boolean deleteContextDefinition(ContextDefinition contextDefinition) throws ContextManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement contextStmt = new SqlStatement(connection, thothDB.getQuery("delete_context"))) {

      contextStmt.setLong("id", contextDefinition.getId());
      contextStmt.executeUpdate();

      int count = contextStmt.executeUpdate();
      commitReload(connection);
      return count == 1;
    } catch (SQLException e) {
      throw new ContextManagerException(e);
    }

  }

  public Map<String, ContextDefinition> getContextDefinitions() throws ContextManagerException {
    Map<String, ContextDefinition> result = contextDefinitions;
    if (result == null) {
      result = doGetContextDefinitions();
      contextDefinitions = result;
    }
    return result;
  }

  public Map<String, ContextDefinition> doGetContextDefinitions() throws ContextManagerException {

    try {
      Map<String, ContextDefinition> result = new HashMap<>();

      try (Connection connection = thothDB.getConnection(); //
          SqlStatement stmt = new SqlStatement(connection, thothDB.getQuery("select_contexts")); //
          ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
          int idx = 1;
          long id = rs.getLong(idx++);
          String name = rs.getString(idx++);
          String repoName = rs.getString(idx++);
          String branch = rs.getString(idx++);
          String libraryRoot = rs.getString(idx++);
          int refreshInterval = rs.getInt(idx++);

          RepositoryDefinition repositoryDefinition = getRepositoryDefinitions().get(repoName.toLowerCase());
          if (repositoryDefinition == null) {
            LOG.error("Could not find RepositoryDefinition named " + repoName + " so ignoring context " + name);
          } else {
            ContextDefinition contextDefinition = new ContextDefinition(repositoryDefinition, name, branch, libraryRoot, refreshInterval);
            contextDefinition.setId(id);
            contextDefinition.setImmutable(false);
            result.put(contextDefinition.getName().toLowerCase(), contextDefinition);
          }
        }
      }
      return result;
    } catch (SQLException e) {
      throw new ContextManagerException(e.getMessage(), e);
    }
  }

  public RepositoryDefinition createRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    return repoDao.createRepositoryDefinition(repositoryDefinition);
  }

  public RepositoryDefinition merge(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    return getRepositoryDefinitions().get(repositoryDefinition.getName().toLowerCase());
  }

  public boolean updateRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    return repoDao.updateRepositoryDefinition(repositoryDefinition);
  }

  public boolean deleteRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    return repoDao.deleteRepositoryDefinition(repositoryDefinition);
  }

  public Map<String, RepositoryDefinition> getRepositoryDefinitions() throws ContextManagerException {
    try {
      Map<String, RepositoryDefinition> result = repositoryDefinitions;
      if (result == null) {
        result = repoDao.getRepositoryDefinitions();
        repositoryDefinitions = result;
      }
      return result;
    } catch (SQLException e) {
      throw new ContextManagerException(e.getMessage(), e);
    }
  }

  public boolean isInUse(RepositoryDefinition repositoryDefinition) throws ContextManagerException {
    return repoDao.isInUse(repositoryDefinition);
  }

}
