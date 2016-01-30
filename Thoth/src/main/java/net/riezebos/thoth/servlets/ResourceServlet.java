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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.beans.BookClassification;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.search.SearchFactory;
import net.riezebos.thoth.content.search.SearchResult;
import net.riezebos.thoth.content.search.Searcher;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.CommitComparator;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.markdown.util.ProcessorError;

public class ResourceServlet extends DocServlet {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceServlet.class);

  private static final long serialVersionUID = 1L;

  @Override
  protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ContentManagerException {

    if (!handleCommand(request, response)) {

      String branch = getBranch(request);
      String path = getPath(request);
      if (("/" + branch + "/").equalsIgnoreCase(NATIVERESOURCES))
        handleNativeResource(path, request, response);
      else if (StringUtils.isBlank(branch) && StringUtils.isBlank(path))
        handleIndex(request, response);
      else if (StringUtils.isBlank(path))
        handleBranchIndex(request, response);
      else {
        handleResource(request, response);
      }
    }
  }

  protected boolean handleCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, ContentManagerException {
    boolean result = true;
    String branch = getBranch(request);
    String cmd = request.getParameter("cmd");

    if ("latestchanges".equalsIgnoreCase(cmd))
      latestChanges(request, response, branch);
    else if ("validationerrors".equalsIgnoreCase(cmd))
      validationErrors(request, response, branch);
    else if ("pull".equalsIgnoreCase(cmd))
      pull(request, response);
    else if ("reindex".equalsIgnoreCase(cmd))
      reindex(request, response);
    else if ("search".equalsIgnoreCase(cmd))
      search(request, response);
    else
      result = false;
    return result;
  }

  protected void search(HttpServletRequest request, HttpServletResponse response)
      throws ContentManagerException, UnsupportedEncodingException, IOException, ServletException {
    String query = request.getParameter("query");
    String branch = getBranch(request);
    Searcher searcher = SearchFactory.getInstance().getSearcher(branch);

    String errorMessage = null;
    List<SearchResult> searchResults = new ArrayList<>();
    try {
      if (StringUtils.isBlank(query))
        errorMessage = "Do you feel lucky?";
      else
        searchResults = searcher.search(query, 10);
    } catch (Exception x) {

      errorMessage = x.getMessage();
      int idx = errorMessage.indexOf(':');
      if (idx != -1)
        errorMessage = errorMessage.substring(idx + 1).trim();
    }

    Map<String, Object> variables = getVariables(request);
    variables.put("errorMessage", errorMessage);
    variables.put("searchResults", searchResults);
    variables.put("query", query);

    if (asJson(request))
      executeJson(variables, response);
    else {
      response.setContentType("text/html;charset=UTF-8");
      String searchTemplate = getSkin(request).getSearchTemplate();
      executeVelocityTemplate(searchTemplate, branch, variables, response);
    }
  }

  protected void handleNativeResource(String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
    // We do not want to expose the entire server; so we require the path to start with REQUIRED_PREFIX (which is 'net/riezebos/thoth/skins/')
    if (!path.startsWith(Configuration.REQUIRED_PREFIX))
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    else {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
      if (is == null) {
        LOG.warn("404 on request for native resource " + request.getRequestURI());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      } else
        IOUtils.copy(is, response.getOutputStream());
    }
  }

  protected void handleResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, BranchNotFoundException, IOException {
    long ms = System.currentTimeMillis();
    String absolutePath = getFileSystemPath(request);
    if (absolutePath == null) {
      LOG.warn("Denied request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      File file = new File(absolutePath);
      if (file.isFile()) {
        FileInputStream fis = new FileInputStream(file);
        IOUtils.copy(fis, response.getOutputStream());
      } else {
        LOG.warn("404 on request " + request.getRequestURI());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      LOG.debug("Handled request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
    }
  }

  protected void handleIndex(HttpServletRequest request, HttpServletResponse response) throws ContentManagerException, IOException, ServletException {
    ContentManager contentManager = getContentManager();

    List<String> branches = contentManager.getBranches();
    Map<String, Object> variables = getVariables(request);
    variables.put("branches", branches);

    if (asJson(request))
      executeJson(variables, response);
    else {
      response.setContentType("text/html;charset=UTF-8");
      String indexTemplate = getSkin(request).getIndexTemplate();
      executeVelocityTemplate(indexTemplate, branches.get(0), variables, response);
    }
  }

  protected void handleBranchIndex(HttpServletRequest request, HttpServletResponse response) throws ContentManagerException, IOException, ServletException {
    ContentManager contentManager = getContentManager();
    String branch = getBranch(request);
    List<Book> books = contentManager.getBooks(branch);
    List<BookClassification> categories = contentManager.getClassification(books, "category", "Unclassified");
    List<BookClassification> audiences = contentManager.getClassification(books, "audience", "Unclassified");
    List<BookClassification> folders = contentManager.getClassification(books, "folder", "Unclassified");

    int maxRevisons = Configuration.getInstance().getBranchMaxRevisions();
    List<Commit> commitList = contentManager.getLatestCommits(branch, null, maxRevisons);
    Collections.sort(commitList, new CommitComparator());

    Map<String, Object> variables = getVariables(request);
    variables.put("books", books);
    variables.put("audiences", audiences);
    variables.put("categories", categories);
    variables.put("folders", folders);
    variables.put("commitList", commitList);

    if (asJson(request))
      executeJson(variables, response);
    else {
      response.setContentType("text/html;charset=UTF-8");
      String indexTemplate = getSkin(request).getBranchIndexTemplate();
      executeVelocityTemplate(indexTemplate, branch, variables, response);
    }
  }

  protected void latestChanges(HttpServletRequest request, HttpServletResponse response, String branch)
      throws ServletException, IOException, ContentManagerException {
    ContentManager contentManager = getContentManager();

    int maxRevisons = Configuration.getInstance().getBranchMaxRevisions();
    List<Commit> commitList = contentManager.getLatestCommits(branch, null, maxRevisons);
    Collections.sort(commitList, new CommitComparator());

    Map<String, Object> variables = getVariables(request);
    variables.put("commitList", commitList);

    if (asJson(request))
      executeJson(variables, response);
    else {
      response.setContentType("text/html;charset=UTF-8");
      String revisionTemplate = getSkin(request).getRevisionTemplate();
      executeVelocityTemplate(revisionTemplate, branch, variables, response);
    }
  }

  protected void validationErrors(HttpServletRequest request, HttpServletResponse response, String branch)
      throws ServletException, IOException, ContentManagerException {

    List<ProcessorError> errors = SearchFactory.getInstance().getIndexer(branch).getValidationErrors();

    Map<String, Object> variables = getVariables(request);
    variables.put("errors", errors);

    if (asJson(request))
      executeJson(variables, response);
    else {
      response.setContentType("text/html;charset=UTF-8");
      String validationTemplate = getSkin(request).getValidationTemplate();
      executeVelocityTemplate(validationTemplate, branch, variables, response);
    }
  }

  protected void pull(HttpServletRequest request, HttpServletResponse response) throws ContentManagerException, ServletException, IOException {
    ContentManager contentManager = getContentManager();
    String log = contentManager.refresh();
    Map<String, Object> variables = getVariables(request);
    variables.put("log", log);

    if (asJson(request))
      executeJson(variables, response);
    else {
      response.setContentType("text/plain;charset=UTF-8");
      response.getWriter().println(log);
    }
  }

  protected void reindex(HttpServletRequest request, HttpServletResponse response) throws ContentManagerException, ServletException, IOException {
    ContentManager contentManager = getContentManager();
    contentManager.reindex();

    Map<String, Object> variables = getVariables(request);
    String log = "Reindex requested. Running in the background now";
    variables.put("log", log);

    if (asJson(request))
      executeJson(variables, response);
    else {
      response.setContentType("text/plain;charset=UTF-8");
      response.getWriter().println(log);
    }
  }
}
