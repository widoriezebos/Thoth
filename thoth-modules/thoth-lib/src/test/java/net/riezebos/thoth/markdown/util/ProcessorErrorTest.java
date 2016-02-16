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
package net.riezebos.thoth.markdown.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProcessorErrorTest {

  @Test
  public void test() {

    ProcessorError error = new ProcessorError(new LineInfo("path/file.txt", 1), "Error message");
    assertEquals("/path/file.txt", error.getCurrentLineInfo().getFile());
    assertEquals(1, error.getCurrentLineInfo().getLine());
    assertEquals("Error message", error.getErrorMessage());
    assertEquals("/path/file.txt(1): Error message", error.getDescription());
  }

}
