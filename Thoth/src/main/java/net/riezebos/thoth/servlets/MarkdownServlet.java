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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.renderers.HtmlRenderer;
import net.riezebos.thoth.renderers.PdfRenderer;
import net.riezebos.thoth.renderers.RawRenderer;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.renderers.Renderer.RenderResult;

public class MarkdownServlet extends DocServlet {
  private static final Logger LOG = LoggerFactory.getLogger(MarkdownServlet.class);

  private static final long serialVersionUID = 1L;

  private Map<String, Renderer> renderers = new HashMap<>();

  public MarkdownServlet() {
    setupRenderers();
  }

  protected void setupRenderers() {
    registerRenderer(new HtmlRenderer());
    registerRenderer(new PdfRenderer());
    registerRenderer(new RawRenderer());
  }

  protected void registerRenderer(Renderer renderer) {
    renderers.put(renderer.getTypeCode().toLowerCase(), renderer);
  }

  protected Renderer getRenderer(String typeCode) {
    if (StringUtils.isBlank(typeCode))
      typeCode = HtmlRenderer.TYPE;
    return renderers.get(typeCode);
  }

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, FileNotFoundException, IOException, ContentManagerException {
    long ms = System.currentTimeMillis();

    Renderer renderer = getRenderer(request.getParameter("output"));
    response.setContentType(renderer.getContentType());
    RenderResult result = renderer.render(getBranch(request), getPath(request), getVariables(request), getSkin(request), response.getOutputStream());
    switch (result) {
    case NOT_FOUND:
      LOG.info("404 on request " + request.getRequestURI());
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      break;
    case FORBIDDEN:
      LOG.warn("Denied request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      break;
    default:
    }
    LOG.debug("Handled request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
  }
}
