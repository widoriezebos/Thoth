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
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.diff_match_patch;
import net.riezebos.thoth.util.diff_match_patch.Diff;

public class DiffServlet extends DocServlet {
  private static final Logger LOG = LoggerFactory.getLogger(DiffServlet.class);

  private static final long serialVersionUID = 1L;

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, FileNotFoundException, IOException, ContentManagerException {
    long ms = System.currentTimeMillis();

    response.setContentType("text/html;charset=UTF-8");
    String absolutePath = getAbsolutePath(request);
    if (absolutePath == null) {
      LOG.warn("Denied request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      String branch = getBranch(request);
      ContentManager contentManager = getContentManager();

      String commitId = request.getParameter("commitId");
      SourceDiff diff = contentManager.getDiff(branch, commitId);

      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
      String timestamp = sdf.format(diff.getTimeModified());

      String newSource = diff.getNewSource();
      String oldSource = diff.getOldSource();
      String commitMessage = diff.getCommitMessage();
      if (commitMessage != null)
        commitMessage = commitMessage.trim();

      diff_match_patch dmp = new diff_match_patch();
      LinkedList<Diff> diffs = dmp.diff_main(oldSource, newSource);
      dmp.diff_cleanupSemantic(diffs);
      String body = prettyPrintHtml(diffs);

      Map<String, Object> variables = getVariables(request);
      variables.put("body", body);
      variables.put("author", diff.getAuthor());
      variables.put("timestamp", timestamp);
      variables.put("commitMessage", commitMessage);

      executeVelocityTemplate(getSkin(request).getDiffTemplate(), branch, variables, response);
    }
  }

  public String prettyPrintHtml(LinkedList<Diff> diffs) {

    StringBuilder html = new StringBuilder();
    int changeCounter = 1;
    for (Diff aDiff : diffs) {
      String text = aDiff.text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "&para;<br>");
      switch (aDiff.operation) {
      case INSERT:
        html.append(getBookmark(changeCounter++)).append("<ins>").append(text).append("</ins>").append("</a>");
        break;
      case DELETE:
        html.append(getBookmark(changeCounter++)).append("<del>").append(text).append("</del>").append("</a>");
        break;
      case EQUAL:
        html.append("<span>").append(text).append("</span>");
        break;
      }
    }

    StringBuilder result = new StringBuilder();
    for (int i = 1; i < changeCounter; i++)
      result.append("<a href=\"#edit" + i + "\">" + i + "</a>&nbsp;");
    if (changeCounter != 1)
      result.append("<br/><br/>");
    result.append(html);
    return result.toString();
  }

  protected String getBookmark(int changeCounter) {
    return "<a name=\"edit" + changeCounter + "\">";
  }

}
