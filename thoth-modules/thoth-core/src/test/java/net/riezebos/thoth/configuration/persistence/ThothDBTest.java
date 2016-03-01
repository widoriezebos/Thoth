package net.riezebos.thoth.configuration.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import net.riezebos.thoth.configuration.PropertyBasedConfiguration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.testutil.ThothTestBase;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;
import net.riezebos.thoth.util.ThothUtil;

public class ThothDBTest extends ThothTestBase {

  @Test
  public void test() throws ConfigurationException, IOException, DatabaseException, UserManagerException {
    PropertyBasedConfiguration configuration = new PropertyBasedConfiguration();
    String defaults = "net/riezebos/thoth/configuration/persistence/dbtest.configuration.properties";
    Properties props = new Properties();
    props.load(getClassPathResource(defaults));

    File dbDir = File.createTempFile("thoth", "db");
    dbDir.delete();
    String databaseFolder = ThothUtil.normalSlashes(dbDir.getAbsolutePath());
    props.setProperty("database.url", databaseFolder);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    props.store(bos, "");

    configuration.load(new ByteArrayInputStream(bos.toByteArray()));
    ThothEnvironment thothEnvironment = new ThothEnvironment();
    thothEnvironment.setConfiguration(configuration);

    ThothDB thothDB = thothEnvironment.getThothDB();
    thothDB.init();
    
    UserManager userManager = thothEnvironment.getUserManager();
    User administrator = userManager.getUser("administrator");
    Group administrators = userManager.getGroup("administrators");
    Group writers = userManager.getGroup("writers");
    Group readers = userManager.getGroup("readers");

    assertTrue(administrators.getMembers().contains(administrator));
    assertTrue(administrators.getPermissions().contains(Permission.PULL));
    assertTrue(administrator.getPermissions().contains(Permission.PULL));
    assertTrue(writers.getPermissions().contains(Permission.META));
    assertFalse(writers.getPermissions().contains(Permission.PULL));
    assertFalse(readers.getPermissions().contains(Permission.PULL));
    assertFalse(readers.getPermissions().contains(Permission.META));

    cleanupTempFolder(dbDir);

  }
}
