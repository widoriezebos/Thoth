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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookClassification implements Comparable<BookClassification> {

  private String name;
  private List<Book> books;

  public BookClassification(String name) {
    this(name, new ArrayList<Book>());
  }

  public BookClassification(String name, List<Book> books) {
    super();
    this.name = name;
    this.books = books;
  }

  public String getName() {
    return name;
  }

  public List<Book> getBooks() {
    return books;
  }

  @Override
  public int compareTo(BookClassification o) {
    return getName().compareTo(o.getName());
  }

  public void sortBooks() {
    Collections.sort(books);
  }

  @Override
  public String toString() {
    return getName();
  }

}
