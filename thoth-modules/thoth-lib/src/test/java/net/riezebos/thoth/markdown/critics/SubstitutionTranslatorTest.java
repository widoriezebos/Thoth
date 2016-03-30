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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class SubstitutionTranslatorTest {

  @Test
  public void test() {
    SubstitutionTranslator insertTranslator = new SubstitutionTranslator(CriticProcessingMode.PROCESS);
    String markdown = "{~~from~>to~~}";
    Matcher matcher = Pattern.compile(CriticMarkupProcessor.SUBSTITUTION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    String translated = insertTranslator.translate(matcher);
    assertEquals("to", translated);

    insertTranslator = new SubstitutionTranslator(CriticProcessingMode.TRANSLATE_ONLY);
    matcher = Pattern.compile(CriticMarkupProcessor.SUBSTITUTION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    translated = insertTranslator.translate(matcher);
    assertEquals("<del>from</del><ins>to</ins>", translated);
  }

}
