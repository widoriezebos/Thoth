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
 */
package net.riezebos.thoth.content.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.context.RepositoryType;
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
    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);

    ClasspathFileSystem cpFS = new ClasspathFileSystem("/net/riezebos/thoth");
    cpFS.registerFile("/markdown.zip", 0L, 0L);
    FileHandle zipFileHandle = cpFS.getFileHandle("markdown.zip");
    ZipFileSystem zfs = new ZipFileSystem(zipFileHandle, "/");

    RepositoryDefinition repodef = new RepositoryDefinition();
    repodef.setLocation("/");
    repodef.setName("testrepos");
    repodef.setType(RepositoryType.FILESYSTEM);

    ContextDefinition contextDef = new ContextDefinition(repodef, "testfs", "branch", "", 0);

    ZipContentManager contentManager = new ZipContentManager(contextDef, thothEnvironment, zfs);

    String log = contentManager.cloneOrPull();
    assertTrue(log.indexOf("testfs: No changes detected") != -1);
    PagedList<Commit> commits = contentManager.getCommits("/", 0, 25);
    assertTrue(commits.getList().isEmpty());
    assertEquals("", contentManager.getDiff("").getOldSource());
    assertFalse(contentManager.supportsVersionControl());

  }
}
