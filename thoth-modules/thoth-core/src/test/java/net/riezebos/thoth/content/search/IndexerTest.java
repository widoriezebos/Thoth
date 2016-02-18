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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.search.util.TestIndexer;
import net.riezebos.thoth.content.search.util.WriteResult;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.util.ProcessorError;
import net.riezebos.thoth.testutil.ThothTestBase;

/*
 * We need to use PowerMock here because for some very intelligent reason (no doubt) the guys from Lucene decided to make the IndexReader.close() method final.
 * Which makes it impossible to mock because it will throw a NPE for any mock. See TestIndexer.getIndexReader() for more.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(IndexReader.class)
public class IndexerTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    ContentManager contentManager = registerTestContentManager("indextest");
    TestIndexer indexer = new TestIndexer(contentManager);
    indexer.index();

    List<Document> addDocumentCalls = indexer.getAddDocumentCalls();
    assertTrue(checkDocumentExists(addDocumentCalls, "/books/Second.book"));
    assertTrue(checkDocumentExists(addDocumentCalls, "/main/Main.md"));

    List<WriteResult> updateDocumentCalls = indexer.getUpdateDocumentCalls();
    assertTrue(checkWriteResultExists(updateDocumentCalls, "/books/Main.book", "used", "false"));
    assertTrue(checkWriteResultExists(updateDocumentCalls, "/main/Table.md", "used", "false"));

    List<ProcessorError> errors = indexer.getErrors();
    assertTrue(checkErrorExists(errors, "Link invalid: imagenotthere/either.png"));
    assertTrue(checkErrorExists(errors, "yup/thisoneis/problematic.md"));

    Map<String, List<String>> indirectReverseIndex = indexer.getIndirectReverseIndex();

    List<String> list = indirectReverseIndex.get("/main/subs/SubOne.md");
    assertEquals(2, list.size());
    assertTrue(list.contains("/books/Main.book"));
    assertTrue(list.contains("/main/Main.md"));

    Map<String, List<String>> directReverseIndex = indexer.getDirectReverseIndex();
    list = directReverseIndex.get("/main/subs/SubOne.md");
    assertEquals(1, list.size());
    assertTrue(list.contains("/main/Main.md"));
  }

  protected boolean checkErrorExists(List<ProcessorError> errors, String string) {
    for (ProcessorError error : errors) {
      if (error.getErrorMessage().indexOf(string) != -1)
        return true;
    }
    return false;
  }

  protected boolean checkWriteResultExists(List<WriteResult> writeResults, String path, String fieldName, String value) {
    for (WriteResult writeResult : writeResults) {
      String termPath = writeResult.getTerm().toString();
      String comparePath = "path:" + path;
      if (comparePath.equals(termPath.toString())) {
        Document document = writeResult.getDocument();
        IndexableField field = document.getField(fieldName);
        if (field != null) {
          String stringValue = field.stringValue();
          if (value.equals(stringValue))
            return true;
        }
      }
    }
    return false;
  }

  protected boolean checkDocumentExists(List<Document> documents, String path) {
    for (Document document : documents) {
      String documentPath = document.get(Indexer.INDEX_PATH);
      if (documentPath.equals(path))
        return true;
    }
    return false;
  }

}
