package net.riezebos.thoth.markdown.critics;

import java.util.regex.Matcher;

public class DeleteTranslator implements TranslateAction {

  private CriticProcessingMode processingMode;

  public DeleteTranslator(CriticProcessingMode processingMode) {
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
      replaceString = "<del>" + value.replaceAll("\n\n", "&nbsp;") + "</del>";
    } else {
      replaceString = "";
    }
    return replaceString;
  }

}
