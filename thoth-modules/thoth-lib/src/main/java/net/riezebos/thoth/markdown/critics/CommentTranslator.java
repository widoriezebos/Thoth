package net.riezebos.thoth.markdown.critics;

import java.util.regex.Matcher;

public class CommentTranslator implements TranslateAction {

  private CriticProcessingMode processingMode;

  public CommentTranslator(CriticProcessingMode processingMode) {
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
      return "<span class=\"critic comment\">" + value + "</span>";
    } else {
      replaceString = "";
    }
    return replaceString;
  }

}
