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
package net.riezebos.thoth.exceptions;

import org.junit.Test;

public class ExceptionTest {

  @Test(expected = CachemanagerException.class)
  public void testCachemanagerException() throws CachemanagerException {
    throw new CachemanagerException(new Exception("Test"));
  }

  @Test(expected = CachemanagerException.class)
  public void testCachemanagerException2() throws CachemanagerException {
    throw new CachemanagerException("Test");
  }

  @Test(expected = CachemanagerException.class)
  public void testCachemanagerException3() throws CachemanagerException {
    throw new CachemanagerException("Test", new Exception("Test"));
  }

  ////////////////

  @Test(expected = ConfigurationException.class)
  public void testConfigurationException() throws ConfigurationException {
    throw new ConfigurationException(new Exception("Test"));
  }

  @Test(expected = ConfigurationException.class)
  public void testConfigurationException2() throws ConfigurationException {
    throw new ConfigurationException("Test");
  }

  @Test(expected = ConfigurationException.class)
  public void testConfigurationException3() throws ConfigurationException {
    throw new ConfigurationException("Test", new Exception("Test"));
  }
  ////////////////

  @Test(expected = ContentManagerException.class)
  public void testContentManagerException() throws ContentManagerException {
    throw new ContentManagerException(new Exception("Test"));
  }

  @Test(expected = ContentManagerException.class)
  public void testContentManagerException2() throws ContentManagerException {
    throw new ContentManagerException("Test");
  }

  @Test(expected = ContentManagerException.class)
  public void testContentManagerException3() throws ContentManagerException {
    throw new ContentManagerException("Test", new Exception("Test"));
  }

  ////////////////

  @Test(expected = IndexerException.class)
  public void testIndexerException() throws IndexerException {
    throw new IndexerException(new Exception("Test"));
  }

  @Test(expected = IndexerException.class)
  public void testIndexerException2() throws IndexerException {
    throw new IndexerException("Test");
  }

  @Test(expected = IndexerException.class)
  public void testIndexerException3() throws IndexerException {
    throw new IndexerException("Test", new Exception("Test"));
  }

  ////////////////

  @Test(expected = SkinManagerException.class)
  public void testSkinManagerException() throws SkinManagerException {
    throw new SkinManagerException(new Exception("Test"));
  }

  @Test(expected = SkinManagerException.class)
  public void testSkinManagerException2() throws SkinManagerException {
    throw new SkinManagerException("Test");
  }

  @Test(expected = SkinManagerException.class)
  public void testSkinManagerException3() throws SkinManagerException {
    throw new SkinManagerException("Test", new Exception("Test"));
  }

  ////////////////

  @Test(expected = RenderException.class)
  public void testRenderException() throws RenderException {
    throw new RenderException(new Exception("Test"));
  }

  @Test(expected = RenderException.class)
  public void testRenderException2() throws RenderException {
    throw new RenderException("Test");
  }
  ////////////////

  @Test(expected = SearchException.class)
  public void testSearchException() throws SearchException {
    throw new SearchException(new Exception("Test"));
  }

  @Test(expected = SearchException.class)
  public void testSearchException2() throws SearchException {
    throw new SearchException("Test");
  }

  ////////////////
  @Test(expected = ContextNotFoundException.class)
  public void testContextNotFoundException() throws ContextNotFoundException {
    throw new ContextNotFoundException("Test");
  }

}
