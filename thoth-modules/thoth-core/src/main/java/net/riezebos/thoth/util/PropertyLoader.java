package net.riezebos.thoth.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.exceptions.ConfigurationException;

public class PropertyLoader {
  private static final Logger LOG = LoggerFactory.getLogger(PropertyLoader.class);

  private Properties properties = new Properties();
  private String propertyFileName;

  protected void load(InputStream inStream) throws ConfigurationException {
    loadDefaults();
    readInputStream(inStream);
  }

  /**
   * Override this method to set default before actually loading any properties
   */
  protected void loadDefaults() {

  }

  protected void readInputStream(InputStream inStream) {
    try {
      properties.load(inStream);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public String getPropertyFileName() {
    return propertyFileName;
  }

  public void setPropertyFileName(String propertyFileName) {
    this.propertyFileName = propertyFileName;
  }

  public String getValue(String key) {
    String value = getValue(key, null);
    if (value == null)
      LOG.error("Property " + key + " is not set in the configuration");
    return value;
  }

  public String getValue(String key, String dflt) {
    String result = null;
    Object value = properties.get(key);
    if (value != null) {
      result = String.valueOf(value).trim();
      if (result.length() == 0)
        result = null;
    }
    return result == null ? dflt : result;
  }
}
