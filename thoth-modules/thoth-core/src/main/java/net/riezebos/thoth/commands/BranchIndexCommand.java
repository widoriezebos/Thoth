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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.beans.BookClassification;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.CommitComparator;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.util.Classifier;
import net.riezebos.thoth.util.PagedList;

public class BranchIndexCommand extends RendererBase implements Command {

  @Override
  public String getTypeCode() {
    return "branchindex";
  }

  public RenderResult execute(String branch, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {
    try {
      ContentManager contentManager = getContentManager();
      Classifier classifier = new Classifier();

      List<Book> books = contentManager.getBooks(branch);
      List<BookClassification> categories = classifier.getClassifications(books, "category", "Unclassified");
      List<BookClassification> audiences = classifier.getClassifications(books, "audience", "Unclassified");
      List<BookClassification> folders = classifier.getClassifications(books, "folder", "Unclassified");

      int pageSize = Configuration.getInstance().getBranchMaxRevisions();
      PagedList<Commit> pagedList = contentManager.getCommits(branch, null, 1, pageSize);
      List<Commit> commitList = pagedList.getList();
      Collections.sort(commitList, new CommitComparator());

      Map<String, Object> variables = new HashMap<>(arguments);
      variables.put("books", books);
      variables.put("audiences", audiences);
      variables.put("categories", categories);
      variables.put("folders", folders);
      variables.put("commitList", commitList);

      if (asJson(arguments))
        executeJson(variables, outputStream);
      else {
        String indexTemplate = skin.getBranchIndexTemplate();
        renderTemplate(indexTemplate, branch, variables, outputStream);
      }

      return RenderResult.OK;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
