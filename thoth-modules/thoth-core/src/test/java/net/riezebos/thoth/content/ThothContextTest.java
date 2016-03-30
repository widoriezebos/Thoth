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
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.impl.ClasspathContentManager;
import net.riezebos.thoth.content.impl.NopContentManager;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.testutil.ThothTestBase;

public class ThothContextTest extends ThothTestBase {

  private static final String CONFIG_FILE = "net/riezebos/thoth/content/contentmanagerfactorytest.configuration.properties";

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException, ConfigurationException {
    Date now = new Date();
    ThothEnvironment thothEnvironment = createThothContext("indextest");

    PropertyBasedConfiguration configuration = new PropertyBasedConfiguration();
    configuration.load(getClassPathResource(CONFIG_FILE));
    thothEnvironment.setConfiguration(configuration);

    ContentManager globalContext = thothEnvironment.getContentManager("");
    assertTrue(globalContext instanceof NopContentManager);
    ContentManager cpContentManager = thothEnvironment.getContentManager("ClasspathContext");
    assertTrue(cpContentManager instanceof ClasspathContentManager);
    ContentManager nopContentManager = thothEnvironment.getContentManager("NothingnessContext");
    assertTrue(nopContentManager instanceof NopContentManager);

    thothEnvironment.touch();
    Date refreshTimestamp = thothEnvironment.getRefreshTimestamp(cpContentManager.getContextName());
    assertTrue(refreshTimestamp.getTime() > now.getTime());
    String log = thothEnvironment.pullAll();
    assertTrue(log.indexOf("Will do nothing") != -1);
  }

}
