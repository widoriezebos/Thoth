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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class FileHandleTest {

  @Test
  public void test() throws IOException {

    BasicFileSystem fs = new BasicFileSystem();
    FileHandle basicFileHandle = fs.getFileHandle("/some/file/name/../other");
    assertFalse(basicFileHandle.exists());
    assertFalse(basicFileHandle.isFile());
    assertFalse(basicFileHandle.isDirectory());
    assertEquals(fs, basicFileHandle.getFileSystem());
    assertEquals("/some/file/other", basicFileHandle.getAbsolutePath());
    assertEquals("/some/file/other", basicFileHandle.getCanonicalPath());
    assertNull(basicFileHandle.list());
    assertNull(basicFileHandle.listFiles());
    FileHandle parentFile = basicFileHandle.getParentFile();
    assertEquals("/some/file", parentFile.getAbsolutePath());
    assertEquals("other", basicFileHandle.getName());
    assertEquals("/some/file/other", basicFileHandle.toString());
    assertEquals(0L, basicFileHandle.lastModified());

    assertTrue(basicFileHandle.equals(basicFileHandle));
    assertTrue(basicFileHandle.compareTo(basicFileHandle) == 0);
    assertFalse(basicFileHandle.equals(parentFile));
    assertFalse(basicFileHandle.equals(null));
    assertFalse(basicFileHandle.equals(""));
    assertTrue(basicFileHandle.hashCode() != parentFile.hashCode());
  }

  @Test(expected = FileNotFoundException.class)
  public void testNotFound() throws IOException {

    BasicFileSystem fs = new BasicFileSystem();
    FileHandle basicFileHandle = fs.getFileHandle("/some/file/name/../other");
    basicFileHandle.getInputStream();
  }

}
