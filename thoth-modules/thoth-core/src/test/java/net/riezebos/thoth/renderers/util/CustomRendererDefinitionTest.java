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
package net.riezebos.thoth.renderers.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CustomRendererDefinitionTest {

  @Test
  public void test() {

    CustomRendererDefinition def1 = new CustomRendererDefinition("ext1", "contentType", "src", "commandLine");
    CustomRendererDefinition def2 = new CustomRendererDefinition("ext1", "contentType", "src", "commandLine");
    CustomRendererDefinition def3 = new CustomRendererDefinition("ext2", "contentType2", "src2", "commandLine2");

    assertEquals(def1, def2);
    assertEquals(def1.hashCode(), def2.hashCode());
    def2.validate();
    assertNotEquals(def3, def2);
  }

}
