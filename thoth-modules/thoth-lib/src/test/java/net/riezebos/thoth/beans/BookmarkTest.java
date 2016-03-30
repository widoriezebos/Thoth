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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BookmarkTest {

  @Test
  public void test() {
    Bookmark bookmark = new Bookmark(1, "one", "Title one");
    assertEquals(1, bookmark.getLevel());
    assertEquals("one", bookmark.getId());
    assertEquals("Title one", bookmark.getTitle());
    assertEquals("Title one", bookmark.toString());
  }

}
