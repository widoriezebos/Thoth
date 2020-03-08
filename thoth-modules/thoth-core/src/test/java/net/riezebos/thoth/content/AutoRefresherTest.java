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
package net.riezebos.thoth.content;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.testutil.ThothTestBase;

public class AutoRefresherTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String contextName = "RefreshContext";
    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    ContentManager contentManager = createTestContentManager(thothEnvironment, contextName);

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
