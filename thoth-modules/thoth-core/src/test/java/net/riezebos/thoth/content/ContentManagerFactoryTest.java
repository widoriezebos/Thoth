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
