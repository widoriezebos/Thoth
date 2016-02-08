package net.riezebos.thoth.markdown.critics;

import java.util.regex.Matcher;

public class SubstitutionTranslator implements TranslateAction {

  private InsertTranslator insertTranslator;
  private DeleteTranslator deleteTranslator;

  public SubstitutionTranslator(CriticProcessingMode processingMode) {
    insertTranslator = new InsertTranslator(processingMode);
    deleteTranslator = new DeleteTranslator(processingMode);
  }

  @Override
  public String translate(Matcher matcher) {
    String delete = matcher.group(1);
    String insert = matcher.group(2);
    return deleteTranslator.translate(delete) + insertTranslator.translate(insert);
  }

  @Override
  public String translate(String value) {
    throw new IllegalArgumentException("Unsupported for SubstitutionTranslator");
  }

}
