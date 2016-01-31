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
package net.riezebos.thoth.renderers;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.parboiled.Parboiled;
import org.pegdown.LinkRenderer;
import org.pegdown.RelaxedParser;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.util.CustomHtmlSerializer;
import net.riezebos.thoth.util.ThothUtil;

public class HtmlRenderer extends RendererBase {
  public static final String TYPE = "html";

  public String getTypeCode() {
    return TYPE;
  }

  public String getContentType() {
    return "text/html;charset=UTF-8";
  }

  public RenderResult render(String branch, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {
    try {
      RenderResult result = RenderResult.OK;

      String absolutePath = getFileSystemPath(branch, path);
      if (absolutePath == null) {
        result = RenderResult.FORBIDDEN;
      } else {
        File file = new File(absolutePath);
        if (file.isFile()) {

          Configuration configuration = Configuration.getInstance();
          MarkDownDocument markdown = getMarkDownDocument(branch, path);
          String markdownSource = markdown.getMarkdown();

          int extensions = configuration.getMarkdownOptions();
          long parseTimeOut = configuration.getParseTimeOut();

          RelaxedParser parser =
              Parboiled.createParser(RelaxedParser.class, extensions, parseTimeOut, RelaxedParser.DefaultParseRunnerProvider, PegDownPlugins.NONE);
          RootNode ast = parser.parse(ThothUtil.wrapWithNewLines(markdownSource.toCharArray()));

          CustomHtmlSerializer serializer = new CustomHtmlSerializer(new LinkRenderer());
          String body = serializer.toHtml(ast);

          Map<String, Object> parameters = new HashMap<>();
          parameters.putAll(markdown.getMetatags());
          parameters.putAll(arguments);
          parameters.put("body", body);

          executeSimpleTemplate(skin.getMarkDownTemplate(), parameters, outputStream);

        } else {
          result = RenderResult.NOT_FOUND;
        }
      }
      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
