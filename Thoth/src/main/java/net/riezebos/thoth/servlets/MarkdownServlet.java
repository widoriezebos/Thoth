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
package net.riezebos.thoth.servlets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.parboiled.Parboiled;
import org.pegdown.LinkRenderer;
import org.pegdown.RelaxedParser;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.CustomHtmlSerializer;
import net.riezebos.thoth.util.ThothUtil;

public class MarkdownServlet extends DocServlet {
  private static final Logger LOG = LoggerFactory.getLogger(MarkdownServlet.class);

  private static final long serialVersionUID = 1L;

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, FileNotFoundException, IOException, ContentManagerException {
    long ms = System.currentTimeMillis();

    response.setContentType("text/html;charset=UTF-8");
    String absolutePath = getFileSystemPath(request);
    if (absolutePath == null) {
      LOG.warn("Denied request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      File file = new File(absolutePath);
      if (file.isFile()) {

        Configuration configuration = Configuration.getInstance();
        MarkDownDocument markdown = getMarkdown(request);
        String markdownSource = markdown.getMarkdown();
        Map<String, Object> metatags = new HashMap<>();
        metatags.putAll(markdown.getMetatags());

        int extensions = configuration.getMarkdownOptions();
        long parseTimeOut = configuration.getParseTimeOut();

        RelaxedParser parser =
            Parboiled.createParser(RelaxedParser.class, extensions, parseTimeOut, RelaxedParser.DefaultParseRunnerProvider, PegDownPlugins.NONE);
        RootNode ast = parser.parse(ThothUtil.wrapWithNewLines(markdownSource.toCharArray()));

        CustomHtmlSerializer serializer = new CustomHtmlSerializer(new LinkRenderer());
        String body = serializer.toHtml(ast);

        PrintWriter writer = response.getWriter();
        metatags.put("body", body);
        String markDownTemplate = getSkin(request).getMarkDownTemplate();
        if (markDownTemplate != null)
          executeSimpleTemplate(writer, markDownTemplate, request, metatags);
        else
          response.getWriter().println(body);

      } else {
        LOG.info("404 on request " + request.getRequestURI());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      LOG.debug("Handled request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
    }
  }
}
