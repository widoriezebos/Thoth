package net.riezebos.thoth.content.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.configuration.RepositoryDefinition;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.markdown.filehandle.ZipFileSystem;
import net.riezebos.thoth.testutil.ThothTestBase;
import net.riezebos.thoth.util.PagedList;

public class ZipContentManagerTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String contextName = "testzip";
    ContentManager testContentManager = registerTestContentManager(contextName);
    Configuration configuration = testContentManager.getConfiguration();

    ClasspathFileSystem cpFS = new ClasspathFileSystem("/net/riezebos/thoth");
    cpFS.registerFile("/markdown.zip", 0L, 0L);
    FileHandle zipFileHandle = cpFS.getFileHandle("markdown.zip");
    ZipFileSystem zfs = new ZipFileSystem(zipFileHandle, "/");

    RepositoryDefinition repodef = new RepositoryDefinition();
    repodef.setLocation("/");
    repodef.setName("testrepos");
    repodef.setType("filesystem");

    ContextDefinition contextDef = new ContextDefinition(repodef, "testfs", "branch", 0);

    ZipContentManager contentManager = new ZipContentManager(contextDef, configuration, zfs);

    String log = contentManager.cloneOrPull();
    assertTrue(log.indexOf("testfs: No changes detected") != -1);
    PagedList<Commit> commits = contentManager.getCommits("/", 0, 25);
    assertTrue(commits.getList().isEmpty());
    assertEquals("", contentManager.getDiff("").getOldSource());
    assertFalse(contentManager.supportsVersionControl());

  }
}
