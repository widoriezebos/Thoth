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

  protected void clear() {
    properties = new Properties();
  }

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
