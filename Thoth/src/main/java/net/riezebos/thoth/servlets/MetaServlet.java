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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.DocumentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.search.Indexer;
import net.riezebos.thoth.content.search.SearchFactory;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.CommitComparator;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.ThothUtil;

public class MetaServlet extends DocServlet {
  private static final Logger LOG = LoggerFactory.getLogger(MetaServlet.class);

  private static final long serialVersionUID = 1L;

  protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, FileNotFoundException, IOException, ContentManagerException {
    long ms = System.currentTimeMillis();

    String absolutePath = getFileSystemPath(request);
    if (absolutePath == null) {
      LOG.warn("Denied request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    } else {
      String relativePath = ThothUtil.stripSuffix(getPath(request), ".meta");
      String branch = getBranch(request);
      ContentManager contentManager = getContentManager();

      MarkDownDocument markDownDocument = contentManager.getMarkDownDocument(branch, relativePath);

      DocumentNode root = markDownDocument.getDocumentStructure();
      List<DocumentNode> documentNodes = root.flatten();
      int maxRevisons = Configuration.getInstance().getFileMaxRevisions();

      Map<String, List<Commit>> commitMap = new HashMap<>();
      List<Commit> commitList = new ArrayList<>();
      for (DocumentNode node : documentNodes) {
        List<Commit> latestCommits = contentManager.getLatestCommits(branch, node.getPath(), maxRevisons);
        commitMap.put(node.getPath(), latestCommits);
        commitList.addAll(latestCommits);
      }
      Collections.sort(commitList, new CommitComparator());

      SearchFactory searchFactory = SearchFactory.getInstance();
      Indexer indexer = searchFactory.getIndexer(branch);
      Map<String, List<String>> reverseIndex = indexer.getReverseIndex(branch, false);
      Map<String, List<String>> reverseIndexIndirect = indexer.getReverseIndex(branch, true);
      List<String> usedBy = reverseIndex.get("/" + relativePath);
      List<String> usedByIndirect = reverseIndexIndirect.get("/" + relativePath);
      Map<String, String> metatags = markDownDocument.getMetatags();
      List<String> metaTagKeys = new ArrayList<>(metatags.keySet());
      Collections.sort(metaTagKeys);

      Map<String, Object> variables = getVariables(request);
      variables.put("document", markDownDocument);
      variables.put("usedBy", usedBy);
      variables.put("usedByIndirect", usedByIndirect);
      variables.put("documentNodes", documentNodes);
      variables.put("commitMap", commitMap);
      variables.put("commitList", commitList);
      variables.put("metatagKeys", metaTagKeys);
      variables.put("metatags", metatags);
      variables.put("errors", markDownDocument.getErrors());

      if (asJson(request))
        executeJson(variables, response);
      else {
        response.setContentType("text/html;charset=UTF-8");
        String metaInformationTemplate = getSkin(request).getMetaInformationTemplate();
        executeVelocityTemplate(metaInformationTemplate, branch, variables, response);
      }
    }
  }
}
