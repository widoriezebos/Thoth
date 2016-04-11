/* Copyright (c) 2016 W.T.J. Riezebos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package net.riezebos.thoth.markdown.critics;

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
