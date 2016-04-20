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

import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.content.ContentManager;

public class TemplateResourceLoader extends ResourceLoader {
  private static final String CLASSPATH_PREFIX = "classpath:";
  private static final Logger LOG = LoggerFactory.getLogger(TemplateResourceLoader.class);

  private static ThreadLocal<ContentManager> contentManager = new ThreadLocal<ContentManager>();
  private static ThreadLocal<String> baseFolder = new ThreadLocal<String>();
  private static ThreadLocal<String> baseFile = new ThreadLocal<String>();

  @Override
  public void init(ExtendedProperties configuration) {
  }

  @Override
  public InputStream getResourceStream(String sourcePath) throws ResourceNotFoundException {
    InputStream is = resolveStream(sourcePath);
    if (is == null) {
      String folder = baseFolder.get();
      String relativePath = ThothUtil.suffix(folder, "/") + ThothUtil.stripSuffix(sourcePath, "/");
      is = resolveStream(relativePath);
    }
    if (is == null) {
      String file = baseFile.get();
      String relativePath = ThothUtil.suffix(ThothUtil.getFolder(file), "/") + ThothUtil.stripSuffix(sourcePath, "/");
      is = resolveStream(relativePath);
    }

    if (is == null)
      throw new ResourceNotFoundException("Could not find " + sourcePath);
    return is;
  }

  protected InputStream resolveStream(String sourcePath) {
    sourcePath = ThothUtil.stripPrefix(sourcePath, CLASSPATH_PREFIX);
    sourcePath = ThothUtil.stripPrefix(sourcePath, "/");

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(sourcePath);
    if (is != null)
      return is;
    try {
      is = getContentManager().getInputStream(sourcePath);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new ResourceNotFoundException(e);
    }
    return is;
  }

  @Override
  public boolean isSourceModified(Resource resource) {
    return false;
  }

  @Override
  public long getLastModified(Resource resource) {
    return 0;
  }

  protected ContentManager getContentManager() {
    ContentManager manager = contentManager.get();
    if (manager == null)
      throw new IllegalArgumentException("ContentManager is not set");
    return manager;
  }

  public static void setContentManager(ContentManager mgr) {
    contentManager.set(mgr);
  }

  public static void setSkinBase(String folder) {
    baseFolder.set(folder);
  }

  public static void setBaseFile(String template) {
    baseFile.set(template);
  }
}
