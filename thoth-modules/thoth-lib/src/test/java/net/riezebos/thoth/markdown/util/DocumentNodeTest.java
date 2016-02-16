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

public class DocumentNodeTest {

  @Test
  public void test() {
    DocumentNode node = new DocumentNode("absolutefilePath", "description", 0, 0);
    DocumentNode node2 = new DocumentNode("/absolutefilePath2#bookmark", null, 2, 2);
    DocumentNode node3 = new DocumentNode("/absolutefilePath3#bookmark", "", 3, 3);
    DocumentNode node4 = new DocumentNode("path/to/absolutefilePath4.md#bookmark", "descr", 4, 4);

    node.addChild(node2);
    node2.addChild(node3);
    node2.addChild(node3);

    assertEquals(2, node2.getLevel());
    assertEquals("/absolutefilePath2", node2.getPath());
    assertEquals(3, node.flatten(true).size());
    assertEquals(4, node.flatten(false).size());

    String string = node.toString();
    assertEquals("/absolutefilePath \n" + //
        "  /absolutefilePath2  line 2\n" + //
        "    /absolutefilePath3  line 3\n" + //
        "    /absolutefilePath3  line 3", string);

    assertEquals("/path/to", node4.getFolder());
    assertEquals("absolutefilePath4.md", node4.getFileName());
    assertEquals("descr", node4.getDescription());
    assertEquals(4, node4.getIncludePosition());
  }

}
