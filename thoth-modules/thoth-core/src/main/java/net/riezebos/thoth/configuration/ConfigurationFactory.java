package net.riezebos.thoth.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.exceptions.ConfigurationException;

public class ConfigurationFactory {
  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
  public static final String CONFIGKEY_DEPRECATED = "configuration";
  public static final String CONFIGKEY = "thoth_configuration";

  private static Configuration _instance;

  public static Configuration getConfiguration() {
    try {
      if (_instance == null) {
        String propertyPath = determinePropertyPath();
        if (propertyPath == null) {
          String msg = "There is no configuration defined. Please set either environment or system property '" + CONFIGKEY + "' and restart";
          LOG.error(msg);
          throw new IllegalArgumentException(msg);
        } else {
          LOG.info("Using " + propertyPath + " for configuration");
          _instance = new PropertyBasedConfiguration(propertyPath);
        }
      }
      return _instance;
    } catch (ConfigurationException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static String determinePropertyPath() {
    String propertyPath = System.getProperty(CONFIGKEY);
    if (propertyPath == null)
      propertyPath = System.getenv(CONFIGKEY);
    if (propertyPath == null)
      propertyPath = System.getProperty(CONFIGKEY_DEPRECATED);
    if (propertyPath == null)
      propertyPath = System.getenv(CONFIGKEY_DEPRECATED);
    return propertyPath;
  }

}
