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

import net.riezebos.thoth.content.Skin;
import net.riezebos.thoth.content.markdown.util.ProcessorError;
import net.riezebos.thoth.content.search.SearchFactory;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.util.RendererBase;

public class ValidationReportCommand extends RendererBase implements Command {

  @Override
  public String getTypeCode() {
    return "validationreport";
  }

  public RenderResult execute(String branch, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {
    try {
      List<ProcessorError> errors = SearchFactory.getInstance().getIndexer(branch).getValidationErrors();

      Map<String, Object> variables = new HashMap<>(arguments);
      variables.put("errors", errors);

      if (asJson(arguments))
        executeJson(variables, outputStream);
      else {
        String validationTemplate = skin.getValidationTemplate();
        executeVelocityTemplate(validationTemplate, branch, variables, outputStream);
      }

      return RenderResult.OK;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
