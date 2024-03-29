/* Copyright (c) 2020 W.T.J. Riezebos
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
package net.riezebos.thoth.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.testutil.ThothTestBase;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;

public class AccessManagerTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String contextName = "test";
    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    UserManager userManager = thothEnvironment.getUserManager();
    ContentManager contentManager = createTestContentManager(thothEnvironment, contextName);
    AccessManager accessManager = new AccessManager(contentManager);

    User administrator = userManager.getUser("administrator");
    User writer = userManager.getUser("writer");
    User reader = userManager.getUser("reader");
    Group anonymous = userManager.getGroup("anonymous");

    assertTrue(accessManager.hasPermission(anonymous, "/public/path/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(anonymous, "/private/readers/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(anonymous, "/private/writers/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(anonymous, "/private/admin/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(anonymous, "/anything/else/something.md", Permission.BASIC_ACCESS));

    assertTrue(accessManager.hasPermission(reader, "/public/path/something.md", Permission.BASIC_ACCESS));
    assertTrue(accessManager.hasPermission(reader, "/private/readers/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(reader, "/private/writers/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(reader, "/private/admin/something.md", Permission.BASIC_ACCESS));
    assertTrue(accessManager.hasPermission(reader, "/anything/else/something.md", Permission.BASIC_ACCESS));

    assertTrue(accessManager.hasPermission(writer, "/public/path/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(writer, "/private/readers/something.md", Permission.BASIC_ACCESS));
    assertTrue(accessManager.hasPermission(writer, "/private/writers/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(writer, "/private/admin/something.md", Permission.BASIC_ACCESS));
    assertTrue(accessManager.hasPermission(writer, "/anything/else/something.md", Permission.BASIC_ACCESS));

    assertTrue(accessManager.hasPermission(administrator, "/public/path/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(administrator, "/private/readers/something.md", Permission.BASIC_ACCESS));
    assertFalse(accessManager.hasPermission(administrator, "/private/writers/something.md", Permission.BASIC_ACCESS));
    assertTrue(accessManager.hasPermission(administrator, "/private/admin/something.md", Permission.BASIC_ACCESS));
    assertTrue(accessManager.hasPermission(administrator, "/anything/else/something.md", Permission.BASIC_ACCESS));

    assertFalse(accessManager.hasPermission(administrator, "/private/trusted/something.md", Permission.BASIC_ACCESS));

  }

}
