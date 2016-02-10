package net.riezebos.thoth.markdown.critics;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class InsertTranslatorTest {

  @Test
  public void test() {
    InsertTranslator insertTranslator = new InsertTranslator(CriticProcessingMode.PROCESS);
    String markdown = "{++ insert ++}";
    Matcher matcher = Pattern.compile(CriticMarkupProcessor.INSERTION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    String translated = insertTranslator.translate(matcher);
    assertEquals(" insert ", translated);

    insertTranslator = new InsertTranslator(CriticProcessingMode.TRANSLATE_ONLY);
    matcher = Pattern.compile(CriticMarkupProcessor.INSERTION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    translated = insertTranslator.translate(matcher);
    assertEquals("<ins> insert </ins>", translated);
  }

}
