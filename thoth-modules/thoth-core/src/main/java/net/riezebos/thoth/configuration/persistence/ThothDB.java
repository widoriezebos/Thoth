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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.configuration.persistence.dbs.DDLExecuter;
import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiom;
import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiomFactory;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
import net.riezebos.thoth.configuration.persistence.dbs.impl.ConnectionWrapper;
import net.riezebos.thoth.configuration.persistence.dbs.impl.DDLException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.util.ThothUtil;

public class ThothDB {
  private static final String CREATE_SCRIPT = "net/riezebos/thoth/db/create_db.ddl";
  private static final String UPGRADE_SCRIPT_PREFIX = "net/riezebos/thoth/db/upgrade_to_";

  private static final Logger LOG = LoggerFactory.getLogger(ThothDB.class);

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
    if (isEmbedded()) {
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

  protected boolean isEmbedded() {
    return "embedded".equalsIgnoreCase(getConfiguration().getDatabaseType());
  }

  protected void initializeSchema(Connection connection) throws SQLException, DDLException, IOException {
    DatabaseIdiom idiom = DatabaseIdiomFactory.getDatabaseIdiom(connection);
    DDLExecuter executer = new DDLExecuter(connection, idiom);
    boolean tableExists = executer.tableExists("thoth_users", getConfiguration().getDatabaseUser());
    if (!tableExists) {
      executer.execute(CREATE_SCRIPT);
      connection.commit();
    }

    performUpgrades(connection);
  }

  protected void performUpgrades(Connection connection) throws SQLException, IOException, DDLException {

    int latestVersion = determineCurrentVersion();
    int currentVersion = latestVersion;

    try (SqlStatement commentStmt = new SqlStatement(connection, getQuery("get_schema_version")); //
        ResultSet rs = commentStmt.executeQuery()) {
      if (rs.next()) {
        currentVersion = rs.getInt(1);
      }
    }

    DatabaseIdiom idiom = DatabaseIdiomFactory.getDatabaseIdiom(connection);
    DDLExecuter executer = new DDLExecuter(connection, idiom);

    for (int i = currentVersion + 1; i <= latestVersion; i++) {
      LOG.info("Upgrading schema to version " + i);
      String upgradeScript = UPGRADE_SCRIPT_PREFIX + String.format("%03d", i) + ".ddl";
      executer.execute(upgradeScript);
      connection.commit();
    }
  }

  private int determineCurrentVersion() throws IOException {
    int result = 0;
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CREATE_SCRIPT);
    String script = ThothUtil.readInputStream(is);
    Pattern versionPattern = Pattern.compile("thoth_version.*values.*\\'thoth\\'\\,\\s*(\\d+)\\s*\\)", Pattern.MULTILINE);
    Matcher matcher = versionPattern.matcher(script);
    if (matcher.find()) {
      String group = matcher.group(1);
      result = Integer.parseInt(group);
    }
    return result;
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

  public void shutdown() {
    if (isEmbedded()) {
      try {
        DriverManager.getConnection("jdbc:derby:;shutdown=true");
      } catch (SQLException e) {
        // I don't like this at all; but by design shutting down Derby will throw an java.sql.SQLException stating "Derby system shutdown."
        // So here comes the dirty code to suppress logging this as an error (because it's not an error)
        if (e.getMessage() == null || e.getMessage().toLowerCase().indexOf("derby system shutdown") == -1)
          LOG.error(e.getMessage(), e);
      }
    }
  }
}
