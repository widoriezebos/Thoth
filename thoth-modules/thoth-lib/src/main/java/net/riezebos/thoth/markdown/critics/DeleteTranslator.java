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
 */
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
