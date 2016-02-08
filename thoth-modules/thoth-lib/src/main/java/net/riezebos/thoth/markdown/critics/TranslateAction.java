package net.riezebos.thoth.markdown.critics;

import java.util.regex.Matcher;

public interface TranslateAction {
  String translate(Matcher matcher);

  String translate(String value);

}
