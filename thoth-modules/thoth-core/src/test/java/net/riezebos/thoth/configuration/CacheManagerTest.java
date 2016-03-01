package net.riezebos.thoth.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    ThothEnvironment thothEnvironment = createThothContext(contextName);
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
    assertEquals(mgr2.getValidationErrors(), indexingContext.getErrors());
    assertEquals(mgr2.getReverseIndex(true), indexingContext.getIndirectReverseIndex());
    assertEquals(mgr2.getReverseIndex(false), indexingContext.getDirectReverseIndex());

  }

}
