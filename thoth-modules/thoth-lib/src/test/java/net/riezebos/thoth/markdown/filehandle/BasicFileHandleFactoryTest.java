package net.riezebos.thoth.markdown.filehandle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.riezebos.thoth.markdown.filehandle.BasicFileHandleFactory;

public class BasicFileHandleFactoryTest {

  @Test
  public void test() {
    BasicFileHandleFactory basicFileHandlerFactory = new BasicFileHandleFactory();
    FileHandle fileHandler = basicFileHandlerFactory.createFileHandle("test");
    assertEquals("test", fileHandler.getName());
  }

}
