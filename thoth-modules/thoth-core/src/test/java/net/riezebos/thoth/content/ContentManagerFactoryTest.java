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
package net.riezebos.thoth.content;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import net.riezebos.thoth.configuration.PropertyBasedConfiguration;
import net.riezebos.thoth.content.impl.ClasspathContentManager;
import net.riezebos.thoth.content.impl.NopContentManager;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.testutil.ThothTestBase;

public class ContentManagerFactoryTest extends ThothTestBase {

  private static final String CONFIG_FILE = "net/riezebos/thoth/content/contentmanagerfactorytest.configuration.properties";

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException, ConfigurationException {
    Date now = new Date();
    registerTestContentManager("indextest");

    ContentManagerFactory contentManagerFactory = ContentManagerFactory.getInstance();

    PropertyBasedConfiguration configuration = new PropertyBasedConfiguration();
    configuration.load(getClassPathResource(CONFIG_FILE));
    contentManagerFactory.setConfiguration(configuration);

    ContentManager globalContext = contentManagerFactory.getContentManager("");
    assertTrue(globalContext instanceof NopContentManager);
    ContentManager cpContentManager = contentManagerFactory.getContentManager("ClasspathContext");
    assertTrue(cpContentManager instanceof ClasspathContentManager);
    ContentManager nopContentManager = contentManagerFactory.getContentManager("NothingnessContext");
    assertTrue(nopContentManager instanceof NopContentManager);

    contentManagerFactory.touch();
    Date refreshTimestamp = contentManagerFactory.getRefreshTimestamp(cpContentManager.getContextName());
    assertTrue(refreshTimestamp.getTime() > now.getTime());
    String log = contentManagerFactory.pullAll();
    assertTrue(log.indexOf("Will do nothing") != -1);
  }

}
