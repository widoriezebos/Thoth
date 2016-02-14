package net.riezebos.thoth.markdown.filehandle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.util.ThothUtil;

public class ZipFileSystemTest {

  private static final String TEST_ZIP = "net/riezebos/thoth/markdown/filehandle/smallzip.zip";

  @Test
  public void test() throws IOException {

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
