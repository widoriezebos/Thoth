package net.riezebos.thoth.markdown.critics;

import java.util.regex.Matcher;

public class InsertTranslator implements TranslateAction {

  private CriticProcessingMode processingMode;

  public InsertTranslator(CriticProcessingMode processingMode) {
    this.processingMode = processingMode;

  }

  @Override
  public String translate(Matcher matcher) {
    String value = matcher.group(1);
    return translate(value);
  }

  @Override
  public String translate(String value) {
    String replaceString = value;
    if (processingMode == CriticProcessingMode.TRANSLATE_ONLY) {
      // Is there a new paragraph followed by new text?
      if (value.startsWith("\n\n") && !value.equals("\n\n")) {
        String paragraphed = "\n\n<ins class='critic' break>&nbsp;</ins>\n\n";
        paragraphed += "<ins>" + value.replaceAll("\n", " ") + "</ins>";
        replaceString = paragraphed;
      }
      // Is the addition just a single new paragraph
      else if (value.equals("\n\n")) {
        replaceString = "\n\n<ins class=\"critic break\">&nbsp;</ins>\n\n";
      }
      // Is it added text followed by a new paragraph?
      if (value.endsWith("\n\n") && !value.equals("\n\n")) {
        replaceString = "<ins>" + value.replaceAll("\n", " ") + "</ins>";
        replaceString += "\n\n<ins class=\"critic break\">&nbsp;</ins>\n\n";
      } else {
        replaceString = "<ins>" + value.replaceAll("\n", " ") + "</ins>";
      }
    }
    return replaceString;
  }

}
