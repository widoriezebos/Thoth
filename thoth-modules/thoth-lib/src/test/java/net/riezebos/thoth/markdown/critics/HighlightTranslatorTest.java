package net.riezebos.thoth.markdown.critics;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class HighlightTranslatorTest {

  @Test
  public void test() {
    HighlightTranslator highlightTranslator = new HighlightTranslator(CriticProcessingMode.PROCESS);
    String markdown = "{-- HIGHLIGHT --}";
    Matcher matcher = Pattern.compile(CriticMarkupProcessor.DELETION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    String translated = highlightTranslator.translate(matcher);
    assertEquals(" HIGHLIGHT ", translated);

    highlightTranslator = new HighlightTranslator(CriticProcessingMode.TRANSLATE_ONLY);
    matcher = Pattern.compile(CriticMarkupProcessor.DELETION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    translated = highlightTranslator.translate(matcher);
    assertEquals("<mark> HIGHLIGHT </mark>", translated);
  }

}
