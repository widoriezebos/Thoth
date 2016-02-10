package net.riezebos.thoth.markdown.critics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class CommentTranslatorTest {

  @Test
  public void test() {
    String markdown = "{>> my comment <<}";
    CommentTranslator commentTranslator = new CommentTranslator(CriticProcessingMode.PROCESS);
    Matcher matcher = Pattern.compile(CriticMarkupProcessor.COMMENT_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    String translated = commentTranslator.translate(matcher);
    assertEquals("", translated);

    commentTranslator = new CommentTranslator(CriticProcessingMode.TRANSLATE_ONLY);
    matcher = Pattern.compile(CriticMarkupProcessor.COMMENT_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    translated = commentTranslator.translate(matcher);
    assertEquals("<span class=\"critic comment\"> my comment </span>", translated);

  }

}
