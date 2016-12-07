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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.util.ThothUtil;

public class ZipFileSystemTest {

  private static final String TEST_ZIP = "net/riezebos/thoth/markdown/filehandle/smallzip.zip";

  @Test
  public void test() throws IOException {
    ClasspathFileSystem cpfactory = new ClasspathFileSystem("/net/riezebos/thoth");
    cpfactory.registerFile("/markdown.zip", 0L, 0L);
    FileHandle zipFileHandle = cpfactory.getFileHandle("markdown.zip");
    ZipFileSystem zfs = new ZipFileSystem(zipFileHandle, "/");

    FileHandle fileHandle = zfs.getFileHandle("/markdown/IncludeProcessor.md");
    FileHandle fileHandle2 = zfs.getFileHandle("markdown/IncludeProcessor.md");
    FileHandle fileHandle3 = zfs.getFileHandle("markdown/NotThere.md");
    FileHandle folderHandle = zfs.getFileHandle("");
    FileHandle folderHandle2 = zfs.getFileHandle("/");
    FileHandle folderHandle3 = zfs.getFileHandle("net/riezebos/Nuts/");
    FileHandle folderHandle4 = zfs.getFileHandle(null);

    assertEquals(fileHandle.getName(), fileHandle2.getName());
    assertEquals("/markdown/IncludeProcessor.md", fileHandle2.toString());
    assertTrue(fileHandle.exists());
    assertFalse(fileHandle.isDirectory());
    assertTrue(fileHandle.isFile());

    assertTrue(fileHandle2.exists());
    assertFalse(fileHandle2.isDirectory());
    assertTrue(fileHandle2.isFile());
    assertTrue(fileHandle2.isFile());
    assertFalse(fileHandle3.exists());
    assertTrue(folderHandle.isDirectory());
    assertTrue(folderHandle2.isDirectory());
    assertFalse(folderHandle3.isFile());
    assertFalse(folderHandle4.isFile());

    assertFalse(zfs.getFileHandle("NotThere").isFile());
    assertFalse(zfs.isFile(null));
    assertFalse(zfs.getFileHandle(null).isFile());
    assertTrue(zfs.getFileHandle("/markdown/IncludeProcessor.md").isFile());
    assertFalse(zfs.getFileHandle("/net/wrong/thoth/markdown/IncludeProcessor.md").isFile());

    FileHandle folder = zfs.getFileHandle("markdown/");
    List<String> lst = Arrays.asList(folder.list());
    assertTrue(lst.contains("IncludeProcessor.md"));
    assertTrue(lst.contains("IncludeProcessorNoToc.md"));
    assertNull(zfs.getFileHandle("nofolder/").list());

    List<FileHandle> lst2 = Arrays.asList(folder.listFiles());
    assertTrue(lst2.contains(fileHandle));

    FileHandle walk = zfs.getFileHandle("/one/two/../../markdown/NotThere.md");

    assertEquals("/markdown/NotThere.md", walk.getCanonicalPath());
    assertEquals("/markdown/NotThere.md", walk.getAbsolutePath());
    assertEquals("/markdown", walk.getParentFile().getAbsolutePath());

    FileHandle check = zfs.getFileHandle("/markdown/check.txt");
    BufferedReader br = new BufferedReader(new InputStreamReader(check.getInputStream()));
    String line = br.readLine();
    assertEquals("check", line);
    br.close();
  }

  @Test
  public void test2() throws IOException {

    ClasspathFileSystem fs = new ClasspathFileSystem();
    FileHandle fileHandle = fs.getFileHandle(TEST_ZIP);
    ZipFileSystem zipfs = new ZipFileSystem(fileHandle);
    FileHandle testHandle = zipfs.getFileHandle("Documentation/Basics/Git concepts.md");
    assertTrue(testHandle.isFile());
    assertTrue(testHandle.exists());
    assertFalse(testHandle.isDirectory());
    String source = ThothUtil.readInputStream(testHandle.getInputStream());
    assertTrue(source.startsWith("# Git concepts"));
    assertTrue(zipfs.getFileHandle("/").isDirectory());
    assertFalse(zipfs.getFileHandle("wrong").isDirectory());
    assertFalse(zipfs.getFileHandle("wrong").isFile());
    assertFalse(zipfs.getFileHandle("wrong").exists());

    FileHandle dirHandle = zipfs.getFileHandle("Documentation/Basics");
    assertTrue(dirHandle.isDirectory());
    assertFalse(dirHandle.isFile());
    assertTrue(dirHandle.exists());
    List<FileHandle> contents = Arrays.asList(dirHandle.listFiles());
    assertTrue(contents.contains(testHandle));

    FileHandle rootHandle = zipfs.getFileHandle("/");
    FileHandle documentation = zipfs.getFileHandle("Documentation");
    FileHandle maindoc = zipfs.getFileHandle("Documentation/Thoth Documentation.md");

    assertTrue(Arrays.asList(rootHandle.listFiles()).contains(documentation));
    assertTrue(Arrays.asList(documentation.listFiles()).contains(maindoc));

  }

  @Test(expected = FileNotFoundException.class)
  public void testNotFound() throws IOException {
    ClasspathFileSystem fs = new ClasspathFileSystem();
    FileHandle fileHandle = fs.getFileHandle(TEST_ZIP);
    ZipFileSystem zipfs = new ZipFileSystem(fileHandle);
    zipfs.getFileHandle("wrong").getInputStream();
  }
}
