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
package net.riezebos.thoth.markdown.filehandle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.util.ThothUtil;

public class BasicFileSystemTest {

  @Test
  public void test() throws IOException {
    long timeOfStart = System.currentTimeMillis();
    ClasspathFileSystem cpFS = new ClasspathFileSystem("/net/riezebos/thoth/markdown/");
    cpFS.registerFiles("/net/riezebos/thoth/resources.lst");

    File tmpFile = File.createTempFile("thoth", "test");
    tmpFile.deleteOnExit();
    String fsroot = ThothUtil.suffix(ThothUtil.normalSlashes(tmpFile.getParent()), "/") + "fstestroot/";
    File fsRootFile = new File(fsroot);
    fsRootFile.mkdirs();
    fsRootFile.deleteOnExit();

    BasicFileSystem basicFS = new BasicFileSystem(fsRootFile.getAbsolutePath());

    FileHandle rootSource = cpFS.getFileHandle("/");
    FileHandle destinationTarget = basicFS.getFileHandle("/");
    destinationTarget.importTree(rootSource);

    /// START OF REGULAR TESTS

    FileHandle fileHandler = basicFS.getFileHandle("/IncludeProcessor.md");
    FileHandle fileHandler2 = basicFS.getFileHandle("IncludeProcessor.md");
    FileHandle fileHandler3 = basicFS.getFileHandle("NotThere.md");
    FileHandle folderHandler = basicFS.getFileHandle("");
    FileHandle folderHandler2 = basicFS.getFileHandle("/");
    FileHandle folderHandler3 = basicFS.getFileHandle("net/riezebos/Nuts/");
    FileHandle folderHandler4 = basicFS.getFileHandle(null);

    long lastModified = fileHandler.lastModified(); // Need to take out MS precision for filestamps
    assertTrue(timeOfStart/1000 <= lastModified/1000);
    assertEquals(fileHandler.getName(), fileHandler2.getName());
    assertEquals("/IncludeProcessor.md", fileHandler2.toString());
    long length = fileHandler2.length();
    assertTrue(length == 459 || length == 482); // Either Linux or Windows with extra CR
    assertTrue(fileHandler.exists());
    assertFalse(fileHandler.isDirectory());
    assertTrue(fileHandler.isFile());

    assertTrue(fileHandler2.exists());
    assertFalse(fileHandler2.isDirectory());
    assertTrue(fileHandler2.isFile());
    assertTrue(fileHandler2.isFile());
    assertFalse(fileHandler3.exists());
    assertTrue(folderHandler.isDirectory());
    assertTrue(folderHandler2.isDirectory());
    assertFalse(folderHandler3.isFile());
    assertFalse(folderHandler4.isFile());

    assertFalse(basicFS.getFileHandle("NotThere").isFile());
    assertFalse(basicFS.isFile(null));
    assertFalse(basicFS.getFileHandle(null).isFile());
    assertTrue(basicFS.getFileHandle("IncludeProcessor.md").isFile());
    assertTrue(basicFS.getFileHandle("/IncludeProcessor.md").isFile());

    FileHandle folder = basicFS.getFileHandle("/");
    List<String> lst = Arrays.asList(folder.list());
    assertTrue(lst.contains("IncludeProcessor.md"));
    assertTrue(lst.contains("IncludeProcessorNoToc.md"));
    assertNull(basicFS.getFileHandle("/nofolder/").list());

    List<FileHandle> lst2 = Arrays.asList(folder.listFiles());
    assertTrue(lst2.contains(fileHandler));

    FileHandle walk = basicFS.getFileHandle("/net/riezebos/thoth/one/two/../../markdown/NotThere.md");

    assertEquals("/net/riezebos/thoth/markdown/NotThere.md", walk.getCanonicalPath());
    assertEquals("/net/riezebos/thoth/markdown/NotThere.md", walk.getAbsolutePath());
    assertEquals("/net/riezebos/thoth/markdown", walk.getParentFile().getAbsolutePath());

    FileHandle check = cpFS.getFileHandle("check.txt");
    BufferedReader br = new BufferedReader(new InputStreamReader(check.getInputStream()));
    String line = br.readLine();
    assertEquals("check", line);
    br.close();

    FileHandleUtil.cleanupCreatedFiles(basicFS);
  }
}
