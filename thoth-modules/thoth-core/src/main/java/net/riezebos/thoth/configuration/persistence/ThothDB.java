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
package net.riezebos.thoth.configuration.persistence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.configuration.persistence.dbs.DDLExecuter;
import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiom;
import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiomFactory;
import net.riezebos.thoth.configuration.persistence.dbs.impl.ConnectionWrapper;
import net.riezebos.thoth.configuration.persistence.dbs.impl.DDLException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.util.ThothUtil;

public class ThothDB {

  private ThothEnvironment thothEnvironment;

  private Map<String, String> queries = null;
  private Map<String, String> drivers = null;

  public ThothDB(ThothEnvironment thothEnvironment) {
    this.thothEnvironment = thothEnvironment;
    setupDriverClass();
  }

  public void init() throws DatabaseException {

    try {
      String databaseType = getConfiguration().getDatabaseType();

      // The following is required to make sure the DriverManager can find the Derby Embedded driver on the classpath
      // even when using a Web Context classloader provided by (for instance) Tomcat
      for (Entry<String, String> entry : drivers.entrySet())
        if (databaseType.toLowerCase().indexOf(entry.getKey()) != -1)
          registerDriver(entry.getValue());

      setupServer();
    } catch (Exception e) {
      throw new DatabaseException(e);
    }
  }

  protected void registerDriver(String driverClassName) throws DatabaseException {

    try {
      Class<?> driverClass = Thread.currentThread().getContextClassLoader().loadClass(driverClassName);
      DriverManager.registerDriver((Driver) driverClass.newInstance());
    } catch (Exception e) {
      throw new DatabaseException(e.getMessage(), e);
    }
  }

  protected void setupServer() throws SQLException, DDLException, IOException {
    try (Connection connection = getConnection()) {
      initializeSchema(connection);
    }
  }

  public Connection getConnection() throws SQLException {
    Properties properties = new Properties();
    String databaseUrl = getConfiguration().getDatabaseUrl();
    if ("embedded".equalsIgnoreCase(getConfiguration().getDatabaseType())) {
      properties.put("databaseName", databaseUrl);
      properties.put("create", "true");
      databaseUrl = "jdbc:derby:";
    }
    properties.put("user", getConfiguration().getDatabaseUser());
    properties.put("password", getConfiguration().getDatabasePassword());

    Connection connection = DriverManager.getConnection(databaseUrl, properties);
    connection.setAutoCommit(false);
    return new ConnectionWrapper(connection);
  }

  protected void initializeSchema(Connection connection) throws SQLException, DDLException, IOException {
    DatabaseIdiom idiom = DatabaseIdiomFactory.getDatabaseIdiom(connection);
    DDLExecuter executer = new DDLExecuter(connection, idiom);
    boolean tableExists = executer.tableExists("thoth_users", getConfiguration().getDatabaseUser());
    if (!tableExists) {
      executer.execute("net/riezebos/thoth/db/create_db.ddl");
      connection.commit();
    }
  }

  public Configuration getConfiguration() {
    return thothEnvironment.getConfiguration();
  }

  public String getQuery(String queryName) {
    if (queries == null) {
      queries = loadQueries();
    }
    String query = queries.get(queryName.toLowerCase());
    if (query == null)
      throw new IllegalArgumentException("Query " + queryName + " not defined");
    return query;
  }

  protected Map<String, String> loadQueries() {
    try {
      Map<String, String> map = new HashMap<>();
      String body = ThothUtil.readInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("net/riezebos/thoth/db/queries.txt"));
      for (String line : body.split(";")) {
        if (!StringUtils.isBlank(line)) {
          int idx = line.indexOf('=');
          if (idx == -1)
            throw new IllegalArgumentException("Line " + line + " does not contain the '=' character");
          String name = line.substring(0, idx).trim();
          String query = line.substring(idx + 1).trim();
          if (map.containsKey(name))
            throw new IllegalArgumentException("Query named " + name + " not unique");
          map.put(name.toLowerCase(), query);
        }
      }
      return map;
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected void setupDriverClass() {
    Map<String, String> result = new HashMap<>();
    result.put("embedded", "org.apache.derby.jdbc.EmbeddedDriver");
    result.put("derby", "org.apache.derby.jdbc.ClientDriver");
    result.put("oracle", "oracle.jdbc.OracleDriver");
    result.put("postgr", "org.postgresql.Driver");
    result.put("h2", "org.h2.Driver");
    result.put("hsql", "org.hsqldb.jdbc.JDBCDriver");
    result.put("mysql", "com.mysql.jdbc.Driver");
    drivers = result;
  }
}
