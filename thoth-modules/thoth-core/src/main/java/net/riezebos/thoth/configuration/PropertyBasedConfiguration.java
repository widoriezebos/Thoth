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
package net.riezebos.thoth.configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.exceptions.ConfigurationException;

public class PropertyBasedConfiguration extends ConfigurationBase implements Configuration {

  private Properties properties = new Properties();

  public PropertyBasedConfiguration() throws ConfigurationException {
  }

  @Override
  public PropertyBasedConfiguration clone() {
    PropertyBasedConfiguration clone = (PropertyBasedConfiguration) super.clone();
    clone.properties = properties;
    return clone;
  }

  @Override
  protected void clear() {
    super.clear();
    properties = getDefaults();
  }

  @Override
  public String getValue(String key) {
    String value = getProperties().getProperty(key);
    return StringUtils.isBlank(value) ? null : value.trim();
  }

  public Properties getProperties() {
    return properties;
  }

  /**
   * Returns an entire branch (folder) as key/value pairs
   */
  protected Map<String, String> mapBranch(String path) {
    String pathSuffixed = path + ".";

    Map<String, String> subst = new HashMap<String, String>();
    getProperties().entrySet().stream()//
        .filter(e -> e.getKey().toString().startsWith(pathSuffixed))//
        .forEach(e -> subst.put(e.getKey().toString().substring(pathSuffixed.length()), e.getValue().toString()));
    return subst;
  }

  public void load(InputStream inStream) throws ConfigurationException {
    try {
      clear();
      getProperties().load(inStream);
      inStream.close();
    } catch (IOException e) {
      throw new ConfigurationException(e);
    }
    String extensionSpec = getImageExtensions();
    for (String ext : extensionSpec.split("\\,"))
      imageExtensions.add(ext.trim().toLowerCase());
    loadRepositoryDefinitions();
    loadContextDefinitions();
  }

  @Override
  public void reload() throws FileNotFoundException, ConfigurationException {
    String sourceSpec = getSourceSpec();
    InputStream inStream;
    if (sourceSpec.startsWith(CLASSPATH_PREFIX))
      inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(sourceSpec.substring(CLASSPATH_PREFIX.length()));
    else {
      if (!sourceSpec.contains("://"))
        sourceSpec = "file://" + sourceSpec;
      try {
        URL url = new URL(sourceSpec);
        inStream = url.openStream();
      } catch (IOException e) {
        throw new FileNotFoundException(MessageFormat.format("Cannot load {1} because of {2}", sourceSpec, e.getMessage()));
      }
    }
    if (inStream == null)
      throw new FileNotFoundException(sourceSpec);
    load(inStream);
  }
}
