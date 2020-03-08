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
package net.riezebos.thoth.content.search.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

public class WriteResult {
  private Term term;
  private Document document;

  public WriteResult(Term term, Document document) {
    this.term = term;
    this.document = document;
  }

  public Term getTerm() {
    return term;
  }

  public Document getDocument() {
    return document;
  }
}
