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
 */package net.riezebos.thoth.commands.util;

import java.util.ArrayList;

import net.riezebos.thoth.commands.SearchCommand;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.search.SearchResult;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.SearchException;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.util.PagedList;

public class TestSearchCommand extends SearchCommand {

  public TestSearchCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  protected PagedList<SearchResult> search(Identity identity, String context, String query, Integer pageNumber, int pageSize)
      throws ContentManagerException, SearchException {
    return new PagedList<SearchResult>(new ArrayList<>(), false);
  }
}
