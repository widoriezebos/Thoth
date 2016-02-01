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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.Skin;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.util.RendererBase;
import net.riezebos.thoth.util.diff_match_patch;
import net.riezebos.thoth.util.diff_match_patch.Diff;

public class DiffCommand extends RendererBase implements Command {

  @Override
  public String getTypeCode() {
    return "diff";
  }

  public RenderResult execute(String branch, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {
    try {
      RenderResult result = RenderResult.OK;
      String absolutePath = getFileSystemPath(branch, path);
      if (absolutePath == null) {
        result = RenderResult.FORBIDDEN;
      } else {
        ContentManager contentManager = getContentManager();
        Configuration configuration = Configuration.getInstance();
        SimpleDateFormat dateFormat = configuration.getDateFormat();
        String commitId = getString(arguments, "commitId");
        SourceDiff diff = contentManager.getDiff(branch, commitId);
        String body = "Diff not found";
        String timestamp = "00-00-0000 00:00:00";
        String commitMessage = "Commit not found";
        String author = "Diff not found";
        LinkedList<Diff> diffs = new LinkedList<>();
        if (diff != null) {
          timestamp = dateFormat.format(diff.getTimeModified());

          String newSource = diff.getNewSource();
          String oldSource = diff.getOldSource();
          commitMessage = diff.getCommitMessage();
          author = diff.getAuthor();
          if (commitMessage != null)
            commitMessage = commitMessage.trim();

          diff_match_patch dmp = new diff_match_patch();
          diffs = dmp.diff_main(oldSource, newSource);
          dmp.diff_cleanupSemantic(diffs);
          body = prettyPrintHtml(diffs);
        }
        boolean asJson = asJson(arguments);

        Map<String, Object> variables = new HashMap<>(arguments);
        if (!asJson)
          variables.put("body", body);
        variables.put("author", author);
        variables.put("timestamp", timestamp);
        variables.put("commitMessage", commitMessage);
        variables.put("diffs", diffs);

        if (asJson)
          executeJson(variables, outputStream);
        else {
          executeVelocityTemplate(skin.getDiffTemplate(), branch, variables, outputStream);
        }
      }
      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  public String prettyPrintHtml(List<Diff> diffs) {

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
