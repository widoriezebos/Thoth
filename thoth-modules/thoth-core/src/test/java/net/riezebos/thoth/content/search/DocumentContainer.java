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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.mockito.stubbing.Answer;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.util.ThothUtil;

public class DocumentContainer {

  private List<Document> docs = new ArrayList<>();
  private List<String> knownDocuments;
  private List<String> knownByNameDocuments;

  public DocumentContainer(ContentManager contentManager) throws IOException {
    knownDocuments = new ArrayList<>();
    knownByNameDocuments = new ArrayList<>();
    docs = new ArrayList<>();
    for (ContentNode node : contentManager.find("*.*", true)) {
      knownDocuments.add(node.getPath());
      knownByNameDocuments.add(ThothUtil.getFileName(node.getPath()).toLowerCase());
    }
    Collections.sort(knownDocuments);
    for (String resourcePath : knownDocuments) {

      String type = Indexer.TYPE_DOCUMENT;
      if (!resourcePath.endsWith(".md"))
        type = Indexer.TYPE_OTHER;
      Document document = new Document();
      document.add(new StringField(Indexer.INDEX_PATH, resourcePath, Field.Store.YES));
      document.add(new StringField(Indexer.INDEX_TYPE, type, Field.Store.YES));
      docs.add(document);
    }
  }

  public int indexOf(String path) {
    int indexOf = knownDocuments.indexOf(path);
    if (indexOf == -1)
      indexOf = knownByNameDocuments.indexOf(path.toLowerCase());
    return indexOf;
  }

  public Answer<TopFieldDocs> getTopDocs() {

    return invocation -> {
      Object[] args = invocation.getArguments();
      ScoreDoc[] scoreDocs = new ScoreDoc[0];
      if (args[0] instanceof TermQuery) {
        TermQuery query = (TermQuery) args[0];
        Term term = query.getTerm();
        String path = term.bytes().utf8ToString();
        scoreDocs = new ScoreDoc[0];
        int idx = indexOf(path);
        if (idx != -1) {
          scoreDocs = new ScoreDoc[1];
          scoreDocs[0] = new ScoreDoc(idx, 1);
        }

      } else
        throw new IllegalArgumentException("Unsupported query test: " + args[0].getClass().getName());
      SortField[] fields = null;
      TotalHits totalHits = new TotalHits(scoreDocs.length, Relation.EQUAL_TO);
      TopFieldDocs topDocs = new TopFieldDocs(totalHits, scoreDocs, fields);
      return topDocs;
    };
  }

  public Answer<Document> getDoc() {
    return invocation -> {
      Object[] args = invocation.getArguments();
      Integer index = (Integer) args[0];
      return docs.get(index);
    };
  }

}
