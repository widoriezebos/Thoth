package net.riezebos.thoth.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ThothCoreUtilTest {

  @Test
  public void test() {
    assertEquals(26, ThothCoreUtil.charLength("asdfghjklqwertyuiopzxcvbnm".getBytes()));
    assertEquals(-1, ThothCoreUtil.charLength(new byte[] {(byte) 0, -1, -2, -3}));

    assertEquals("This &lt;tag&gt; &amp; with a% has to be <b>escaped</b>&lt;/tag&gt;",
        ThothCoreUtil.escapeHtmlExcept("b", "This <tag> & with a% has to be <b>escaped</b></tag>"));
  }

}
