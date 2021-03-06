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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.CommitComparator;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.util.PagedList;

public class RevisionsCommand extends RendererBase implements Command {

  public RevisionsCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "revisions";
  }

  @Override
  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {
      ContentManager contentManager = getContentManager(contextName);
      if (!contentManager.getAccessManager().hasPermission(identity, path, Permission.REVISION))
        return RenderResult.FORBIDDEN;

      Integer pageNumber = getInteger(arguments, "page");
      if (pageNumber == null)
        pageNumber = 1;

      int pageSize = getThothEnvironment().getConfiguration().getContextMaxRevisions();
      PagedList<Commit> pagedList = contentManager.getCommits(null, pageNumber, pageSize);
      List<Commit> commitList = pagedList.getList();
      boolean hasMore = pagedList.hasMore();
      Collections.sort(commitList, new CommitComparator());

      Map<String, Object> variables = new HashMap<>(arguments);
      variables.put("commitList", commitList);
      variables.put("page", pageNumber);
      variables.put("hasmore", hasMore);

      render(skin.getSkinBaseFolder(), skin.getRevisionTemplate(), contextName, arguments, variables, outputStream);

      return RenderResult.OK;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
