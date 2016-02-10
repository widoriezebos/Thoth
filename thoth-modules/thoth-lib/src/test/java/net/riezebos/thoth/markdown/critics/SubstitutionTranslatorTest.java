package net.riezebos.thoth.markdown.critics;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class SubstitutionTranslatorTest {

  @Test
  public void test() {
    SubstitutionTranslator insertTranslator = new SubstitutionTranslator(CriticProcessingMode.PROCESS);
    String markdown = "{~~from~>to~~}";
    Matcher matcher = Pattern.compile(CriticMarkupProcessor.SUBSTITUTION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    String translated = insertTranslator.translate(matcher);
    assertEquals("to", translated);

    insertTranslator = new SubstitutionTranslator(CriticProcessingMode.TRANSLATE_ONLY);
    matcher = Pattern.compile(CriticMarkupProcessor.SUBSTITUTION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    translated = insertTranslator.translate(matcher);
    assertEquals("<del>from</del><ins>to</ins>", translated);
  }

}
