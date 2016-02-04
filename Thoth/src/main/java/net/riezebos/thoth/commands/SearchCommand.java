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
package net.riezebos.thoth.commands;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.content.search.SearchFactory;
import net.riezebos.thoth.content.search.SearchResult;
import net.riezebos.thoth.content.search.Searcher;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.util.PagedList;

public class SearchCommand extends RendererBase implements Command {

  @Override
  public String getTypeCode() {
    return "search";
  }

  public RenderResult execute(String branch, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {
    try {
      List<SearchResult> searchResults = new ArrayList<>();
      String errorMessage = null;
      String query = getString(arguments, "query");
      Integer pageNumber = getInteger(arguments, "page");
      if (pageNumber == null)
        pageNumber = 1;
      boolean hasMore = false;

      try {
        Searcher searcher = SearchFactory.getInstance().getSearcher(branch);
        if (StringUtils.isBlank(query))
          errorMessage = "Do you feel lucky?";
        else {
          int pageSize = Configuration.getInstance().getMaxSearchResults();
          PagedList<SearchResult> pagedList = searcher.search(query, pageNumber, pageSize);
          searchResults.addAll(pagedList.getList());
          hasMore = pagedList.hasMore();
        }
      } catch (Exception x) {

        errorMessage = x.getMessage();
        int idx = errorMessage.indexOf(':');
        if (idx != -1)
          errorMessage = errorMessage.substring(idx + 1).trim();
      }

      Map<String, Object> variables = new HashMap<>(arguments);
      variables.put("page", pageNumber);
      variables.put("hasmore", hasMore);
      variables.put("errorMessage", errorMessage);
      variables.put("searchResults", searchResults);
      variables.put("query", query);

      if (asJson(arguments))
        executeJson(variables, outputStream);
      else {
        String searchTemplate = skin.getSearchTemplate();
        renderTemplate(searchTemplate, branch, variables, outputStream);
      }
      return RenderResult.OK;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

}
