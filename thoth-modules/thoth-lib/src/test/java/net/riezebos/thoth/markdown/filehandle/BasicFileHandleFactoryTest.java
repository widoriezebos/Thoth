package net.riezebos.thoth.markdown.filehandle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.riezebos.thoth.markdown.filehandle.BasicFileSystem;

public class BasicFileHandleFactoryTest {

  @Test
  public void test() {
    BasicFileSystem basicFileHandlerFactory = new BasicFileSystem();
    FileHandle fileHandler = basicFileHandlerFactory.getFileHandle("test");
    assertEquals("test", fileHandler.getName());
  }

}
