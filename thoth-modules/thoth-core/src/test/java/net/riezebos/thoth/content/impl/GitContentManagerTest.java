package net.riezebos.thoth.content.impl;

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

    ThothEnvironment thothEnvironment = createThothContext(contextName);
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
