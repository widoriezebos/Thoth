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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.ThothUtil;

public class PdfServlet extends DocServlet {
  private static final Logger LOG = LoggerFactory.getLogger(PdfServlet.class);

  private static final long serialVersionUID = 1L;

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, FileNotFoundException, IOException, ContentManagerException {
    long ms = System.currentTimeMillis();

    response.setContentType("application/pdf");
    String absolutePath = getAbsolutePath(request);
    if (absolutePath == null) {
      LOG.warn("Denied request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      String branch = getBranch(request);
      String path = branch + "/" + getPath(request);
      Configuration configuration = Configuration.getInstance();
      String url = (configuration.getLocalHostUrl() + path).replaceAll(" ", "%20");
      if (url.toLowerCase().endsWith(".pdf"))
        url = url.substring(0, url.length() - 4);

      File tempFile = File.createTempFile("docservers", ".pdf");

      Map<String, Object> args = getVariables(request);
      args.put("url", url);
      args.put("output", tempFile.getAbsolutePath());
      String command = ThothUtil.replaceKeywords(configuration.getPdfCommand(), args);

      execute(command);

      FileInputStream fis = new FileInputStream(tempFile);
      IOUtils.copy(fis, response.getOutputStream());
      fis.close();

      tempFile.delete();
    }
  }

  protected String execute(String command) throws IOException {
    String result = "";
    String line;
    Process p = Runtime.getRuntime().exec(command);
    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
    while ((line = input.readLine()) != null) {
      result += line + "\n";
    }
    input.close();
    return result;
  }
}
