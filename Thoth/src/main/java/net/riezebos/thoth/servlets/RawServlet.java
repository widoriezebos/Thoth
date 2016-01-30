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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.exceptions.ContentManagerException;

public class RawServlet extends DocServlet {
  private static final Logger LOG = LoggerFactory.getLogger(RawServlet.class);

  private static final long serialVersionUID = 1L;

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, FileNotFoundException, IOException, ContentManagerException {
    long ms = System.currentTimeMillis();

    response.setContentType("text/plain;charset=UTF-8");
    String absolutePath = getAbsolutePath(request);
    if (absolutePath == null) {
      LOG.warn("Denied request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      response.getWriter().print(getMarkdown(request, ".raw").getMarkdown());
    }
  }
}
