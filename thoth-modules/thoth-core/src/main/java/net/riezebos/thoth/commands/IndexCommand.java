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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.AccessManager;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;

public class IndexCommand extends RendererBase implements Command {
  private static final Logger LOG = LoggerFactory.getLogger(IndexCommand.class);

  public IndexCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "index";
  }

  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {
      RenderResult result = RenderResult.OK;
      List<String> contexts = new ArrayList<String>();
      List<String> problems = new ArrayList<String>();

      for (String ctxt : getThothEnvironment().getContextManager().getContexts()) {
        try {
          ContentManager contentManager = getThothEnvironment().getContentManager(ctxt);
          AccessManager accessManager = contentManager.getAccessManager();
          if (accessManager.hasPermission(identity, "/", Permission.BASIC_ACCESS))
            contexts.add(ctxt);
        } catch (ContentManagerException e) {
          problems.add(ctxt+": "+e.getMessage());
          LOG.error(e.getMessage(), e);
        }
      }

      Map<String, Object> variables = new HashMap<>(arguments);
      variables.put("contexts", contexts);
      variables.put("problems", problems);

      if (asJson(arguments))
        executeJson(variables, outputStream);
      else {
        String indexTemplate = skin.getIndexTemplate();
        String context = contexts.isEmpty() ? null : contexts.get(0);
        renderTemplate(indexTemplate, context, variables, outputStream);
      }
      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
