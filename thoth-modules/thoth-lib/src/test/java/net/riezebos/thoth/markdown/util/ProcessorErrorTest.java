package net.riezebos.thoth.markdown.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProcessorErrorTest {

  @Test
  public void test() {

    ProcessorError error = new ProcessorError(new LineInfo("path/file.txt", 1), "Error message");
    assertEquals("/path/file.txt", error.getCurrentLineInfo().getFile());
    assertEquals(1, error.getCurrentLineInfo().getLine());
    assertEquals("Error message", error.getErrorMessage());
    assertEquals("/path/file.txt(1): Error message", error.getDescription());
  }

}
