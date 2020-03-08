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
