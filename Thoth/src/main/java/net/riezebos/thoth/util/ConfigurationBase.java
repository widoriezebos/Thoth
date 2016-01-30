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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationBase {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationBase.class);

  private Properties properties = new Properties();
  private String propertyFileName;

  public ConfigurationBase() {
  }

  public void load(String propertyFileName) {
    try {
      this.propertyFileName = propertyFileName;
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
