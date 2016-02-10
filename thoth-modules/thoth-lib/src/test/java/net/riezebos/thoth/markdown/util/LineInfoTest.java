package net.riezebos.thoth.markdown.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class LineInfoTest {

  @Test
  public void test() {
    LineInfo info = new LineInfo("/some/file/path.txt", 1);
    assertEquals("/some/file/path.txt", info.getFile());
    assertEquals(1, info.getLine());
  }

}
