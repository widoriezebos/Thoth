package net.riezebos.thoth.markdown.filehandle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class BasicFileHandleTest {

  @Test
  public void test() throws IOException {

    BasicFileHandle basicFileHandle = new BasicFileHandle("/some/file/name/../other");
    assertFalse(basicFileHandle.exists());
    assertFalse(basicFileHandle.isFile());
    assertFalse(basicFileHandle.isDirectory());
    assertEquals("/some/file/name/../other", basicFileHandle.getAbsolutePath());
    assertEquals("/some/file/other", basicFileHandle.getCanonicalPath());
    assertNull(basicFileHandle.list());
    FileHandle parentFile = basicFileHandle.getParentFile();
    assertEquals("/some/file/name/..", parentFile.getAbsolutePath());
    assertEquals("other", basicFileHandle.getName());
    assertEquals("/some/file/name/../other", basicFileHandle.toString());
    assertEquals(0L, basicFileHandle.lastModified());
  }

  @Test(expected = FileNotFoundException.class)
  public void testNotFound() throws IOException {

    BasicFileHandle basicFileHandle = new BasicFileHandle(new File("/some/file/name/../other"));
    basicFileHandle.getInputStream();

  }

}
