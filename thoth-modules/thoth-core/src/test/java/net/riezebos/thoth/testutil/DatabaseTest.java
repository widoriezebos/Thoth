package net.riezebos.thoth.testutil;

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
