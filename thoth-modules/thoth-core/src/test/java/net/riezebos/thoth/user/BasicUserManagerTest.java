package net.riezebos.thoth.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.DDLExecuter;
import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiom.DatabaseFlavour;
import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiomFactory;
import net.riezebos.thoth.configuration.persistence.dbs.impl.DerbyDatabaseIdiom;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.testutil.DatabaseTest;

public class BasicUserManagerTest extends DatabaseTest {

  @Test
  public void test() throws ConfigurationException, IOException, DatabaseException, UserManagerException, SQLException {

    ThothDB thothDB = getThothDB();
    try {
      try (Connection connection = thothDB.getConnection()) {
        DDLExecuter executer = new DDLExecuter(connection, new DerbyDatabaseIdiom());
        assertTrue(executer.tableExists("thoth_users", null));
        assertEquals(DatabaseFlavour.DERBY, DatabaseIdiomFactory.getDatabaseIdiom(connection).getFlavour());
      }

      UserManager userManager = getThothEnvironment().getUserManager();

      List<Group> groups = userManager.listGroups();
      List<User> users = userManager.listUsers();

      User administrator = userManager.getUser("administrator");
      Group administrators = userManager.getGroup("thoth_administrators");
      Group writers = userManager.getGroup("thoth_writers");
      Group readers = userManager.getGroup("thoth_readers");

      assertTrue(groups.contains(administrators));
      assertTrue(groups.contains(writers));
      assertTrue(groups.contains(readers));

      assertTrue(users.contains(administrator));

      assertTrue(administrators.getMembers().contains(administrator));
      assertTrue(administrators.getPermissions().contains(Permission.PULL));
      assertTrue(administrator.getPermissions().contains(Permission.PULL));
      assertTrue(writers.getPermissions().contains(Permission.META));
      assertFalse(writers.getPermissions().contains(Permission.PULL));
      assertFalse(readers.getPermissions().contains(Permission.PULL));
      assertFalse(readers.getPermissions().contains(Permission.META));

      User someUser = userManager.createUser(new User("wido"));
      List<User> newList = userManager.listUsers();
      List<User> collected = newList.stream().filter(p -> p.getIdentifier().equals("wido")).collect(Collectors.toList());
      assertTrue(collected.size() == 1);
      User user = collected.get(0);

      user.setFirstname("Wido");
      userManager.updateUser(user);
      newList = userManager.listUsers();
      collected = newList.stream().filter(p -> "Wido".equals(p.getFirstname())).collect(Collectors.toList());
      assertTrue(collected.size() == 1);

      assertFalse(someUser.getPermissions().contains(Permission.META));
      userManager.createMembership(writers, someUser);
      someUser = userManager.merge(someUser);
      assertTrue(someUser.getPermissions().contains(Permission.META));
      userManager.deleteMembership(writers, readers);
      someUser = userManager.merge(someUser);
      assertFalse(readers.getPermissions().contains(Permission.META));

      userManager.deleteIdentity(user);
      newList = userManager.listUsers();
      collected = newList.stream().filter(p -> p.getIdentifier().equals("wido")).collect(Collectors.toList());
      assertTrue(collected.size() == 0);
    } finally {
      cleanupTempFolder();
    }
  }
}
