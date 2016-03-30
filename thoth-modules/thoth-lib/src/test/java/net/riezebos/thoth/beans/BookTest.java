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
package net.riezebos.thoth.beans;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class BookTest {

  @Test
  public void test() {
    Book book = new Book("name", "/some/path/name.md");
    Book book2 = new Book("name.txt", "name.md");
    Book book3 = new Book("SomeTitle.md", "/some/path/name.md");

    Map<String, String> meta = new HashMap<String, String>();
    meta.put("a", "1");
    meta.put("b", "2");
    meta.put("c", "3");
    book.setMetaTags(meta);

    assertEquals("name", book.getName());
    assertEquals("/some/path", book.getFolder());
    assertEquals("name.txt", book2.getName());
    assertEquals("/some/path/name.md", book.getPath());
    assertEquals("name.md", book2.getPath());
    assertEquals("SomeTitle", book3.getTitle());

    assertTrue(book.compareTo(book2) < 1);

    assertEquals("1", book.getMetaTag("a"));
    assertArrayEquals(new String[] {"a", "b", "c"}, book.getMetaTagKeys().toArray(new String[0]));

    assertEquals(book.toString(), book.getPath());
  }

}
