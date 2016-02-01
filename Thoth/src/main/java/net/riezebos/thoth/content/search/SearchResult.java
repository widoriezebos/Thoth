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
package net.riezebos.thoth.content.search;

import java.util.ArrayList;
import java.util.List;

import net.riezebos.thoth.content.markdown.util.DocumentNode;

public class SearchResult {

  private String document;
  private List<Fragment> fragments = new ArrayList<>();
  private List<DocumentNode> bookReferences = new ArrayList<>();

  public String getDocument() {
    return document;
  }

  public void setDocument(String document) {
    this.document = document;
  }

  @Override
  public String toString() {
    return getDocument();
  }

  public void addFragment(Fragment fragment) {
    fragments.add(fragment);
  }

  public List<Fragment> getFragments() {
    return fragments;
  }

  public void addBookReference(DocumentNode book) {
    bookReferences.add(book);
  }

  public List<DocumentNode> getBookReferences() {
    return bookReferences;
  }

}
