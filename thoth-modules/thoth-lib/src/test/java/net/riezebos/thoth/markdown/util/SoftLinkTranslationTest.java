package net.riezebos.thoth.markdown.util;

import static org.junit.Assert.*;

import java.util.regex.Matcher;

import org.junit.Test;

public class SoftLinkTranslationTest {

  @Test
  public void test() {
    String replacePattern = "/library/datamodel/$1/Class.md";
    SoftLinkTranslation softLinkTranslation = new SoftLinkTranslation("~*", replacePattern);
    Matcher matcher = softLinkTranslation.getPattern().matcher("~Test");
    assertTrue(matcher.matches());
    assertEquals(replacePattern, softLinkTranslation.getReplacePattern());
    assertEquals("^\\~(.*?)$", softLinkTranslation.getPattern().toString());
    assertEquals("^\\~(.*?)$", softLinkTranslation.toString());
    
  }

}
