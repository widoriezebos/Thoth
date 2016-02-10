package net.riezebos.thoth.markdown.critics;

import java.util.regex.Matcher;

public class HighlightTranslator implements TranslateAction {

  private CriticProcessingMode processingMode;

  public HighlightTranslator(CriticProcessingMode processingMode) {
    this.processingMode = processingMode;

  }

  @Override
  public String translate(Matcher matcher) {
    String value = matcher.group(1);
    return translate(value);
  }

  @Override
  public String translate(String value) {
    String replaceString;
    if (processingMode == CriticProcessingMode.TRANSLATE_ONLY) {
      return "<mark>" + value + "</mark>";
    } else {
      replaceString = value;
    }
    return replaceString;
  }

}
