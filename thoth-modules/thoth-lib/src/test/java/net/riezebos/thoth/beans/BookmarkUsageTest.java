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

import net.riezebos.thoth.markdown.util.LineInfo;

public class BookmarkUsageTest {

  @Test
  public void test() {
    BookmarkUsage usage = new BookmarkUsage();
    usage.setBookMark("mark1");
    usage.setCurrentLineInfo(new LineInfo("file", 1));
    assertEquals(1, usage.getCurrentLineInfo().getLine());
    assertEquals("/file", usage.getCurrentLineInfo().getFile());
    assertEquals("/file(1): #mark1", usage.toString());
    
    BookmarkUsage usage2 = new BookmarkUsage("mark2");
    assertEquals("mark2", usage2.getBookmark());
  }

}
