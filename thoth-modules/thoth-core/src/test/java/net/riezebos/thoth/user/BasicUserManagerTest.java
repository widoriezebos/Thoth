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
 */package net.riezebos.thoth.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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

      Group someGroup = userManager.createGroup(new Group("somegroup"));
      someGroup.grantPermission(Permission.BASIC_ACCESS);
      someGroup.grantPermission(Permission.BROWSE);
      someGroup.grantPermission(Permission.COMMENT);
      userManager.updatePermissions(someGroup);

      // This is tricky; but to make sure we check a real round trip through the DB we need to make
      // sure we get caching out of the loop. Hence: use another (new) userManager

      BasicUserManager otherUserManager = new BasicUserManager(getThothEnvironment());
      Group check = otherUserManager.getGroup("somegroup");
      assertTrue(check.getPermissions().contains(Permission.BASIC_ACCESS));
      assertTrue(check.getPermissions().contains(Permission.BROWSE));
      assertTrue(check.getPermissions().contains(Permission.COMMENT));

      someGroup.revokePermission(Permission.COMMENT);
      userManager.updatePermissions(someGroup);

      otherUserManager = new BasicUserManager(getThothEnvironment());
      check = otherUserManager.getGroup("somegroup");
      assertTrue(check.getPermissions().contains(Permission.BASIC_ACCESS));
      assertTrue(check.getPermissions().contains(Permission.BROWSE));
      assertFalse(check.getPermissions().contains(Permission.COMMENT));

      userManager.deleteIdentity(someGroup);
      otherUserManager = new BasicUserManager(getThothEnvironment());
      assertNull(otherUserManager.getGroup("somegroup"));

    } finally {
      cleanupTempFolder();
    }
  }
}
