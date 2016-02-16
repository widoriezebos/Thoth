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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class BookClassificationTest {

  @Test
  public void test() {
    BookClassification bookClassification = new BookClassification("class1");

    List<Book> books = new ArrayList<Book>();
    Book bookB = new Book("b", "path1");
    Book bookA = new Book("a", "path2");
    books.add(bookB);
    books.add(bookA);

    BookClassification bookClassification2 = new BookClassification("class2", books);
    bookClassification2.sortBooks();

    assertEquals("class1", bookClassification.getName());
    assertEquals("class1", bookClassification.toString());
    assertEquals(bookA, bookClassification2.getBooks().get(0));

    assertTrue(bookClassification.compareTo(bookClassification2) < 1);
  }

}
