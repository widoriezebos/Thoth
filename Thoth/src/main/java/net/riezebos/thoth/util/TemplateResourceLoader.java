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
import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateResourceLoader extends ResourceLoader {
  private static final String CLASSPATH_PREFIX = "classpath:";
  private static final Logger LOG = LoggerFactory.getLogger(TemplateResourceLoader.class);

  @Override
  public void init(ExtendedProperties configuration) {
  }

  @Override
  public InputStream getResourceStream(String sourcePath) throws ResourceNotFoundException {

    if (sourcePath.startsWith(CLASSPATH_PREFIX)) {
      sourcePath = sourcePath.substring(CLASSPATH_PREFIX.length());
    }
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(sourcePath);
    if (is != null)
      return is;
    try {
      return new FileInputStream(sourcePath);

    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new ResourceNotFoundException(e);
    }
  }

  @Override
  public boolean isSourceModified(Resource resource) {
    return false;
  }

  @Override
  public long getLastModified(Resource resource) {
    return 0;
  }
}
