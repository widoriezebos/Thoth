/* Copyright (c) 2020 W.T.J. Riezebos
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

import org.junit.Test;

public class CriticMarkupProcessorTest {

  @Test
  public void test() {
    CriticMarkupProcessor criticMarkupProcessor = new CriticMarkupProcessor();

    String source = "This is {++added ++}and then {--deleted --}" + //
        "or later {~~changed~>tosomething~~}{>>commented<<} and then {==HIGHLIGHTED==} done.";

    String result = criticMarkupProcessor.processCritics(source, CriticProcessingMode.DO_NOTHING);
    assertEquals(source, result);

    String processed = "This is added and then " + //
        "or later tosomething and then HIGHLIGHTED done.";

    result = criticMarkupProcessor.processCritics(source, CriticProcessingMode.PROCESS);
    assertEquals(processed, result);

    String source2 = "Nothing matches";
    result = criticMarkupProcessor.processCritics(source2, CriticProcessingMode.PROCESS);
    assertEquals(source2, result);

  }

}
