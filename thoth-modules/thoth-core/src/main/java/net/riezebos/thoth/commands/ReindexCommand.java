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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.configuration.ThothContext;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RendererBase;

public class ReindexCommand extends RendererBase implements Command {

  public ReindexCommand(ThothContext thothContext) {
    super(thothContext);
  }

  @Override
  public String getTypeCode() {
    return "reindex";
  }

  @Override
  public String getContentType(Map<String, Object> arguments) {
    return "text/plain;charset=UTF-8";
  }

  public RenderResult execute(String context, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {
    try {

      if (StringUtils.isBlank(context))
        getContext().reindexAll();
      else {
        reindex(context);
      }
      Map<String, Object> variables = new HashMap<>(arguments);
      String log = "Reindex reuested. Running in the background";
      variables.put("log", log);

      if (asJson(arguments))
        executeJson(variables, outputStream);
      else {
        try (PrintWriter writer = new PrintWriter(outputStream)) {
          writer.println(log);
        }
      }

      return RenderResult.OK;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  protected void reindex(String context) throws ContentManagerException {
    ContentManager contentManager = getContentManager(context);
    contentManager.reindex();
  }
}
