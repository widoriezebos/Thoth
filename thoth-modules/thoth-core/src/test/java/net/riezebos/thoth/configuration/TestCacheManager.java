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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.CachemanagerException;

public class TestCacheManager extends CacheManager {

  Map<String, ByteArrayOutputStream> resources = new HashMap<String, ByteArrayOutputStream>();
  boolean mockReverseIndexes = true;

  public TestCacheManager(ContentManager contentManager) {
    super(contentManager);
  }

  @Override
  protected InputStream createInputStream(String resourcePath) throws FileNotFoundException {
    ByteArrayOutputStream byteArrayOutputStream = resources.get(resourcePath);
    if (byteArrayOutputStream == null)
      return null;

    ByteArrayInputStream bis = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    return bis;
  }

  @Override
  protected OutputStream createOutputStream(String resourcePath) throws FileNotFoundException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    resources.put(resourcePath, bos);
    return bos;
  }

  @Override
  protected void deleteFile(String fileName) {
    resources.remove(fileName);
  }

  public Map<String, ByteArrayOutputStream> getResources() {
    return resources;
  }

  public void setResources(Map<String, ByteArrayOutputStream> resources) {
    this.resources = resources;
  }

  public void setMockReverseIndexes(boolean mockReverseIndexes) {
    this.mockReverseIndexes = mockReverseIndexes;
  }

  @Override
  public Map<String, List<String>> getReverseIndex(boolean indirect) throws CachemanagerException {
    if (!mockReverseIndexes)
      return super.getReverseIndex(indirect);
    else
      return indirect ? getReverseIndexIndirect() : getReverseIndex();
  }

  protected Map<String, List<String>> getReverseIndex() {
    Map<String, List<String>> result = new HashMap<>();
    result.put("/main/subs/SubOne.md", Arrays.asList(new String[] {"/main/Main.md"}));
    result.put("/main/Main.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Second.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Second.book"}));
    result.put("/main/Third.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Fourth.md", Arrays.asList(new String[] {"/books/Second.book"}));
    return result;
  }

  protected Map<String, List<String>> getReverseIndexIndirect() {
    Map<String, List<String>> result = new HashMap<>();
    result.put("/main/subs/SubOne.md", Arrays.asList(new String[] {"/main/Main.md", "/books/Main.book"}));
    result.put("/main/Main.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Second.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Second.book"}));
    result.put("/main/Third.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Main.book"}));
    result.put("/main/Fourth.md", Arrays.asList(new String[] {"/books/Second.book"}));
    return result;
  }

}
