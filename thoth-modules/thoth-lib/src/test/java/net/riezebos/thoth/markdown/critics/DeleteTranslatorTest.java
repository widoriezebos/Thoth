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

public class DeleteTranslatorTest {

  @Test
  public void test() {
    DeleteTranslator deleteTranslator = new DeleteTranslator(CriticProcessingMode.PROCESS);
    String markdown = "{-- delete this --}";
    Matcher matcher = Pattern.compile(CriticMarkupProcessor.DELETION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    String translated = deleteTranslator.translate(matcher);
    assertEquals("", translated);

    deleteTranslator = new DeleteTranslator(CriticProcessingMode.TRANSLATE_ONLY);
    matcher = Pattern.compile(CriticMarkupProcessor.DELETION_PATTERN, Pattern.DOTALL).matcher(markdown);
    assertTrue(matcher.find(0));
    translated = deleteTranslator.translate(matcher);
    assertEquals("<del> delete this </del>", translated);

  }

}
