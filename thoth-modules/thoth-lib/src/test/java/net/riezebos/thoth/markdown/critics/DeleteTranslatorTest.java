package net.riezebos.thoth.markdown.critics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class DeleteTranslatorTest {

  @Test
  public void test() {
    DeleteTranslator deleteTranslator = new DeleteTranslator(CriticProcessingMode.PROCESS);
    String markdown = "{-- delete this --}";
    Matcher matcher = Pattern.compile(CriticMarkupProcessor.DELETION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    String translated = deleteTranslator.translate(matcher);
    assertEquals("", translated);

    deleteTranslator = new DeleteTranslator(CriticProcessingMode.TRANSLATE_ONLY);
    matcher = Pattern.compile(CriticMarkupProcessor.DELETION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    translated = deleteTranslator.translate(matcher);
    assertEquals("<del> delete this </del>", translated);

  }

}
