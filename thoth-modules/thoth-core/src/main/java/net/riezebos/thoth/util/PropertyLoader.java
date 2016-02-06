package net.riezebos.thoth.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyLoader {
  private static final Logger LOG = LoggerFactory.getLogger(PropertyLoader.class);

  private Properties properties = new Properties();
  private String propertyFileName;

  public void load(String propertyFileName) {
    try {
      setPropertyFileName(propertyFileName);
      FileInputStream inStream = new FileInputStream(propertyFileName);
      load(inStream);
    } catch (IOException e) {
      String msg = "Could not load configuration from '" + propertyFileName + "' because of: " + e.getMessage();
      LOG.error(msg, e);
      throw new IllegalArgumentException(msg);
    }
  }

  protected void load(InputStream inStream) {
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
