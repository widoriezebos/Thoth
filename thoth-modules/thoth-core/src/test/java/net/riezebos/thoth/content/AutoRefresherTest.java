package net.riezebos.thoth.content;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.testutil.ThothTestBase;

public class AutoRefresherTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    ContentManager contentManager = registerTestContentManager("RefreshContext");

    AutoRefresher refresher = new AutoRefresher(0, contentManager);
    Date now = new Date();
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
    }
    assertTrue(contentManager.getLatestRefresh().getTime() > now.getTime());
    refresher.cancel();
  }

}
