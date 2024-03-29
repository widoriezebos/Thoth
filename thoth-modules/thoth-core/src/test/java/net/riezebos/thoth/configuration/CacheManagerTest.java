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
package net.riezebos.thoth.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.search.IndexingContext;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.util.LineInfo;
import net.riezebos.thoth.markdown.util.ProcessorError;
import net.riezebos.thoth.testutil.ThothTestBase;

public class CacheManagerTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    List<ProcessorError> errors = new ArrayList<>();
    Map<String, List<String>> reverseIndex = new HashMap<String, List<String>>();
    Map<String, List<String>> reverseIndexIndirect = new HashMap<String, List<String>>();

    String contextName = "TestCacheContext";
    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    ContentManager contentManager = createTestContentManager(thothEnvironment, contextName);
    TestCacheManager mgr = new TestCacheManager(contentManager);
    mgr.setMockReverseIndexes(false);
    mgr.cacheErrors(errors);
    mgr.cacheReverseIndex(true, reverseIndexIndirect);
    mgr.cacheReverseIndex(false, reverseIndex);

    assertNotNull(mgr.getFileLock());
    assertEquals(contentManager, mgr.getContentManager());
    assertEquals(reverseIndex, mgr.getReverseIndex(false));
    assertEquals(reverseIndexIndirect, mgr.getReverseIndex(true));
    assertEquals(errors, mgr.getValidationErrors());

    IndexingContext indexingContext = new IndexingContext();
    indexingContext.getDirectReverseIndex().put("one", Arrays.asList(new String[] {"two", "three"}));
    indexingContext.getIndirectReverseIndex().put("one", Arrays.asList(new String[] {"two", "three", "four"}));
    indexingContext.getErrors().add(new ProcessorError(new LineInfo("file", 0), "There was a glitch in the matrix"));
    indexingContext.getReferencedLocalResources().add("/some/local/Resource.bin");

    mgr.persistIndexingContext(indexingContext);

    TestCacheManager mgr2 = new TestCacheManager(contentManager);
    mgr2.setMockReverseIndexes(false);
    mgr2.setResources(mgr.getResources());

    List<ProcessorError> errorList = new ArrayList<>(indexingContext.getErrors());
    Collections.sort(errorList);

    assertEquals(mgr2.getValidationErrors(), errorList);
    assertEquals(mgr2.getReverseIndex(true), indexingContext.getIndirectReverseIndex());
    assertEquals(mgr2.getReverseIndex(false), indexingContext.getDirectReverseIndex());

  }

}
