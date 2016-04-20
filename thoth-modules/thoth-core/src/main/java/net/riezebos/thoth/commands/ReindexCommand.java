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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;

public class ReindexCommand extends RendererBase implements Command {

  public ReindexCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "reindex";
  }

  @Override
  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {
      ContentManager contentManager = getContentManager(contextName);
      if (!contentManager.getAccessManager().hasPermission(identity, path, Permission.REINDEX))
        return RenderResult.FORBIDDEN;

      if (StringUtils.isBlank(contextName))
        getThothEnvironment().reindexAll();
      else {
        reindex(contentManager);
      }
      Map<String, Object> variables = new HashMap<>(arguments);
      String log = "Reindex reuested. Running in the background";
      variables.put("title", "Reindex report");
      variables.put("log", log);

      render(skin.getSkinBaseFolder(), skin.getLogTemplate(), contextName, arguments, variables, outputStream);
      
      return RenderResult.OK;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  protected void reindex(ContentManager contentManager) {
    contentManager.reindex();
  }

}
