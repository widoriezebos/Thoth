package net.riezebos.thoth.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class PropertyBasedConfigurationTest {

  @Test
  public void test() throws ConfigurationException, ContextNotFoundException {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    InputStream is = contextClassLoader.getResourceAsStream("net/riezebos/thoth/configuration/test.configuration.properties");
    PropertyBasedConfiguration config = new PropertyBasedConfiguration();
    config.load(is);
    config.validate();

    assertTrue(compareSet("marked,book,index", config.getBookExtensions()));
    assertTrue(compareSet("category,audience,folder", config.getContextIndexClassifications()));
    assertTrue(compareSet("Context1", config.getContexts()));
    assertTrue(compareSet("marked,book,index,md", config.getDocumentExtensions()));
    assertEquals("Context1", config.getContextDefinition("Context1").getName());
    assertEquals("/path/to/your/thoth/workspace/", config.getWorkspaceLocation());
    assertEquals(25, config.getContextMaxRevisions());
    assertEquals("pdf", config.getCustomRenderers().get(0).getExtension());
    assertEquals("dd-MM-yyyy", config.getDateFormatMask());
    assertEquals("01-01-1970", config.getDateFormat().format(new Date(0L)));
    assertEquals("dd-MM-yyyy HH:mm:ss", config.getTimestampFormatMask());
    assertEquals("01-01-1970 01:00:00", config.getTimestampFormat().format(new Date(0L)));
    assertEquals(4000, config.getParseTimeOut());
    assertEquals(2098159, config.getMarkdownOptions());
    assertEquals("http://localhost:8080/", config.getLocalHostUrl());
    assertEquals(true, config.appendErrors());
    assertEquals(10, config.getFileMaxRevisions());
    assertEquals(8080, config.getEmbeddedServerPort());
    assertEquals("localhost", config.getEmbeddedServerName());
    assertEquals(30, config.getEmbeddedIdleTimeout());
    assertEquals("md,book,marked,txt", config.getIndexExtensions());
    assertNull(config.getMainIndexSkinContext());
    assertEquals("SimpleSkin", config.getDefaultSkin());
    assertTrue(config.isPrettyPrintJson());
    assertEquals(25, config.getMaxSearchResults());
    assertEquals(3, config.getMaxHeaderNumberingLevel());
    assertTrue(config.isImageExtension("png"));
    assertFalse(config.isImageExtension(null));
    assertTrue(config.isFragment("somefile.md"));
    assertFalse(config.isFragment(".hidden"));
    assertFalse(config.isFragment("nothing"));

    assertTrue(config.isBook("somefile.book"));
    assertFalse(config.isBook("somefile.md"));
    assertFalse(config.isBook(".hidden"));
    assertFalse(config.isBook("nothing"));

    assertTrue(config.isResource("nothing"));
    assertTrue(config.isResource("nothing.properties"));
  }

  private boolean compareSet(String spec, Collection<String> list) {
    Set<String> set = new HashSet<>();
    for (String value : spec.split("\\,")) {
      set.add(value.trim());
    }
    return set.containsAll(list) && list.containsAll(set);
  }

}
