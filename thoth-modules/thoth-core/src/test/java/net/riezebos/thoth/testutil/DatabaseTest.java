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
 */package net.riezebos.thoth.testutil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import net.riezebos.thoth.configuration.PropertyBasedConfiguration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.util.ThothUtil;

public class DatabaseTest extends ThothTestBase {

  private File dbDir;
  ThothEnvironment thothEnvironment;

  public ThothDB getThothDB() throws DatabaseException, IOException, ConfigurationException {
    PropertyBasedConfiguration configuration = new PropertyBasedConfiguration();
    String defaults = "net/riezebos/thoth/configuration/persistence/dbtest.configuration.properties";
    Properties props = new Properties();
    props.load(getClassPathResource(defaults));

    dbDir = File.createTempFile("thoth", "db");
    dbDir.delete();
    String databaseFolder = ThothUtil.normalSlashes(dbDir.getAbsolutePath());
    props.setProperty("database.url", databaseFolder);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    props.store(bos, "");

    configuration.load(new ByteArrayInputStream(bos.toByteArray()));
    thothEnvironment = new ThothEnvironment();
    thothEnvironment.setConfiguration(configuration);

    ThothDB thothDB = thothEnvironment.getThothDB();
    thothDB.init();
    return thothDB;
  }

  public ThothEnvironment getThothEnvironment() {
    return thothEnvironment;
  }

  public void cleanupTempFolder() throws IOException {
    if (dbDir != null)
      cleanupTempFolder(dbDir);
  }

}
