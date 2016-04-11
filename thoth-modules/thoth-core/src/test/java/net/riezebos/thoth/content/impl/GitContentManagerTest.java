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
 */package net.riezebos.thoth.content.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.junit.Test;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.impl.util.TestGitContentManager;
import net.riezebos.thoth.content.versioncontrol.Revision.Action;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.context.RepositoryType;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.testutil.ThothTestBase;

public class GitContentManagerTest extends ThothTestBase {

  @Test
  public void test() throws ContentManagerException {
    String contextName = "testgit";

    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    RepositoryDefinition repodef = new RepositoryDefinition();
    repodef.setLocation("http://someserver/somerepos.git");
    repodef.setName("testrepos");
    repodef.setUsername("username");
    repodef.setPassword("password");
    repodef.setType(RepositoryType.GIT);

    ContextDefinition contextDef = new ContextDefinition(repodef, "testgit", "branch", "", 0);
    GitContentManager contentManager = new TestGitContentManager(contextDef, thothEnvironment);

    contentManager.disableAutoRefresh();
    assertTrue(contentManager.supportsVersionControl());
    assertEquals("branch", contentManager.getBranch());
    assertEquals(Action.ADD, contentManager.translateAction(ChangeType.ADD));
    assertEquals(Action.COPY, contentManager.translateAction(ChangeType.COPY));
    assertEquals(Action.DELETE, contentManager.translateAction(ChangeType.DELETE));
    assertEquals(Action.MODIFY, contentManager.translateAction(ChangeType.MODIFY));
    assertEquals(Action.RENAME, contentManager.translateAction(ChangeType.RENAME));
    assertEquals("/some/workspace/testgit/", contentManager.getContextFolder());
  }

}
