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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.configuration.persistence.dbs.DDLExecuter;
import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiom;
import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiomFactory;
import net.riezebos.thoth.configuration.persistence.dbs.impl.DDLException;
import net.riezebos.thoth.exceptions.DatabaseException;

public class ThothDB {

  private ThothEnvironment thothEnvironment;

  public ThothDB(ThothEnvironment thothEnvironment)

  {
    this.thothEnvironment = thothEnvironment;
  }

  public void init() throws DatabaseException {

    try {
      setupServer();
    } catch (Exception e) {
      throw new DatabaseException(e);
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

    return DriverManager.getConnection(databaseUrl, properties);
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

  protected Configuration getConfiguration() {
    return thothEnvironment.getConfiguration();
  }

}
