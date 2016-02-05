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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RendererBase;

public class BrowseCommand extends RendererBase implements Command {

  @Override
  public String getTypeCode() {
    return "browse";
  }

  public RenderResult execute(String branch, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {

    try {
      RenderResult result = RenderResult.OK;
      List<ContentNode> contentNodes = getContentManager().list(branch, path);
      boolean asJson = asJson(arguments);

      Map<String, Object> variables = new HashMap<>(arguments);
      variables.put("contentNodes", contentNodes);
      boolean atRoot = StringUtils.isBlank(path);
      variables.put("atRoot", atRoot);

      if (asJson)
        executeJson(variables, outputStream);
      else {
        renderTemplate(skin.getBrowseTemplate(), branch, variables, outputStream);
      }

      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
