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
package net.riezebos.thoth.content.search;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class SearchFactory {
  private static SearchFactory instance = new SearchFactory();

  private SearchFactory() {
  }

  public static SearchFactory getInstance() {
    return instance;
  }

  public Searcher getSearcher(String branch) throws ContentManagerException {
    return new Searcher(branch);
  }

  public Indexer getIndexer(String branch) throws ContentManagerException {
    ContentManager contentManager = ContentManagerFactory.getContentManager();
    return new Indexer(contentManager, branch);
  }

}
