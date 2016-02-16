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

public class ClasspathFileHandleFactoryTest {

  @Test
  public void test() throws IOException {
    ClasspathFileSystem factory = new ClasspathFileSystem();
    factory.registerFiles("/net/riezebos/thoth/resources.lst");
    FileHandle fileHandler = factory.getFileHandle("/net/riezebos/thoth/markdown/IncludeProcessor.md");
    FileHandle fileHandler2 = factory.getFileHandle("net/riezebos/thoth/markdown/IncludeProcessor.md");
    FileHandle fileHandler3 = factory.getFileHandle("net/riezebos/thoth/markdown/NotThere.md");
    FileHandle folderHandler = factory.getFileHandle("net/riezebos/thoth");
    FileHandle folderHandler2 = factory.getFileHandle("net/riezebos/thoth/");
    FileHandle folderHandler3 = factory.getFileHandle("net/riezebos/Nuts/");
    FileHandle folderHandler4 = factory.getFileHandle(null);

    assertEquals(20000L, fileHandler.lastModified());
    assertEquals(fileHandler.getName(), fileHandler2.getName());
    assertEquals("/net/riezebos/thoth/markdown/IncludeProcessor.md", fileHandler2.toString());
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

    assertFalse(factory.getFileHandle("NotThere").isFile());
    assertFalse(factory.isFile(null));
    assertFalse(factory.getFileHandle(null).isFile());
    assertTrue(factory.getFileHandle("/net/riezebos/thoth/markdown/IncludeProcessor.md").isFile());
    assertFalse(factory.getFileHandle("/net/wrong/thoth/markdown/IncludeProcessor.md").isFile());

    FileHandle folder = factory.getFileHandle("net/riezebos/thoth/markdown/");
    List<String> lst = Arrays.asList(folder.list());
    assertTrue(lst.contains("IncludeProcessor.md"));
    assertTrue(lst.contains("IncludeProcessorNoToc.md"));
    assertNull(factory.getFileHandle("net/riezebos/thoth/nofolder/").list());

    List<FileHandle> lst2 = Arrays.asList(folder.listFiles());
    assertTrue(lst2.contains(fileHandler));

    FileHandle walk = factory.getFileHandle("/net/riezebos/thoth/one/two/../../markdown/NotThere.md");

    assertEquals("/net/riezebos/thoth/markdown/NotThere.md", walk.getCanonicalPath());
    assertEquals("/net/riezebos/thoth/markdown/NotThere.md", walk.getAbsolutePath());
    assertEquals("/net/riezebos/thoth/markdown", walk.getParentFile().getAbsolutePath());

    FileHandle check = factory.getFileHandle("/net/riezebos/thoth/markdown/check.txt");
    BufferedReader br = new BufferedReader(new InputStreamReader(check.getInputStream()));
    String line = br.readLine();
    assertEquals("check", line);
    br.close();

  }

  @Test(expected = IllegalArgumentException.class)
  public void testFail() throws IOException {
    ClasspathFileSystem factory = new ClasspathFileSystem();
    factory.registerFiles("/net/riezebos/thoth/wrong.lst");

  }

  @Test(expected = FileNotFoundException.class)
  public void testFailInput() throws IOException {
    ClasspathFileSystem factory = new ClasspathFileSystem();
    FileHandle check = factory.getFileHandle("/net/riezebos/thoth/markdown/notthere.txt");
    check.getInputStream();

  }
}
