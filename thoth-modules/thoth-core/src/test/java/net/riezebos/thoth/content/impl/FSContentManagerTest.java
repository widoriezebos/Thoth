package net.riezebos.thoth.content.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.markdown.filehandle.FileHandleUtil;
import net.riezebos.thoth.markdown.filehandle.FileSystem;
import net.riezebos.thoth.testutil.ThothTestBase;
import net.riezebos.thoth.util.PagedList;

public class FSContentManagerTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    FSContentManager contentManager = createTempFSContentManager("testgit");
    ClasspathFileSystem classpathFileSystem = getClasspathFileSystem();

    FileHandle rootSource = classpathFileSystem.getFileHandle("/");
    FileHandle destinationTarget = contentManager.getFileSystem().getFileHandle("/");
    destinationTarget.importTree(rootSource);

    String log = contentManager.cloneOrPull();
    assertTrue(log.indexOf("Changes detected, reindex requested") != -1);
    PagedList<Commit> commits = contentManager.getCommits("/", 0, 25);
    assertTrue(commits.getList().isEmpty());
    assertEquals("", contentManager.getDiff("").getOldSource());
    assertFalse(contentManager.supportsVersionControl());

    FileSystem targetFileSystem = destinationTarget.getFileSystem();
    FileHandleUtil.cleanupCreatedFiles(targetFileSystem);
  }
}
