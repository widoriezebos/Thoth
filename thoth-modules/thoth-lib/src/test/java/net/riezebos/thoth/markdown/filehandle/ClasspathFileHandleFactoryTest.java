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
    ClasspathFileHandleFactory factory = new ClasspathFileHandleFactory();
    factory.registerFiles("/net/riezebos/thoth/resources.lst");
    FileHandle fileHandler = factory.createFileHandle("/net/riezebos/thoth/markdown/IncludeProcessor.md");
    FileHandle fileHandler2 = factory.createFileHandle("net/riezebos/thoth/markdown/IncludeProcessor.md");
    FileHandle fileHandler3 = factory.createFileHandle("net/riezebos/thoth/markdown/NotThere.md");
    FileHandle folderHandler = factory.createFileHandle("net/riezebos/thoth");
    FileHandle folderHandler2 = factory.createFileHandle("net/riezebos/thoth/");
    FileHandle folderHandler3 = factory.createFileHandle("net/riezebos/Nuts/");
    FileHandle folderHandler4 = factory.createFileHandle(null);

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

    assertFalse(factory.isFile("NotThere"));
    assertFalse(factory.isFile(null));
    assertTrue(factory.isFile("/net/riezebos/thoth/markdown/IncludeProcessor.md"));
    assertFalse(factory.isFile("/net/wrong/thoth/markdown/IncludeProcessor.md"));

    FileHandle folder = factory.createFileHandle("net/riezebos/thoth/markdown/");
    List<String> lst = Arrays.asList(folder.list());
    assertTrue(lst.contains("IncludeProcessor.md"));
    assertTrue(lst.contains("IncludeProcessorNoToc.md"));
    assertNull(factory.createFileHandle("net/riezebos/thoth/nofolder/").list());

    FileHandle walk = factory.createFileHandle("/net/riezebos/thoth/one/two/../../markdown/NotThere.md");

    assertEquals("/net/riezebos/thoth/markdown/NotThere.md", walk.getCanonicalPath());
    assertEquals("/net/riezebos/thoth/one/two/../../markdown/NotThere.md", walk.getAbsolutePath());
    assertEquals("/net/riezebos/thoth/one/two/../../markdown", walk.getParentFile().getAbsolutePath());
    
    FileHandle check = factory.createFileHandle("/net/riezebos/thoth/markdown/check.txt");
    BufferedReader br = new BufferedReader(new InputStreamReader(check.getInputStream()));
    String line = br.readLine();
    assertEquals("check", line);
    br.close();

  }

  @Test(expected = IllegalArgumentException.class)
  public void testFail() throws IOException {
    ClasspathFileHandleFactory factory = new ClasspathFileHandleFactory();
    factory.registerFiles("/net/riezebos/thoth/wrong.lst");

  }

  @Test(expected = FileNotFoundException.class)
  public void testFailInput() throws IOException {
    ClasspathFileHandleFactory factory = new ClasspathFileHandleFactory();
    FileHandle check = factory.createFileHandle("/net/riezebos/thoth/markdown/notthere.txt");
    check.getInputStream();

  }
}
