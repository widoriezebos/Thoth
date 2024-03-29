/* Copyright (c) 2020 W.T.J. Riezebos
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.util.diff_match_patch.Diff;

public class DiffCommand extends RendererBase implements Command {

  public DiffCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "diff";
  }

  @Override
  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {
      ContentManager contentManager = getContentManager(contextName);
      if (!contentManager.getAccessManager().hasPermission(identity, path, Permission.DIFF))
        return RenderResult.FORBIDDEN;

      RenderResult result = RenderResult.OK;
      Configuration configuration = getThothEnvironment().getConfiguration();
      SimpleDateFormat dateFormat = configuration.getTimestampFormat();
      String commitId = getString(arguments, "commitId");
      SourceDiff diff = contentManager.getDiff(commitId);
      String body = "Diff not found";
      String timestamp = "00-00-0000 00:00:00";
      String commitMessage = "Commit not found";
      String author = "Diff not found";
      List<Diff> diffs = new ArrayList<>();
      if (diff != null) {
        timestamp = dateFormat.format(diff.getTimeModified());
        commitMessage = diff.getCommitMessage();
        author = diff.getAuthor();
        if (commitMessage != null)
          commitMessage = commitMessage.trim();

        diffs = diff.getDiffs();
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

      render(skin.getSkinBaseFolder(), skin.getDiffTemplate(), contextName, arguments, variables, outputStream);

      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  public String prettyPrintHtml(List<Diff> diffs) {

    StringBuilder html = new StringBuilder();
    int changeCounter = 1;
    for (Diff aDiff : diffs) {
      String text = aDiff.text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
    if (!html.toString().endsWith("\n"))
      html.append("\n");
    StringBuilder withLineNumbers = new StringBuilder();
    int idx = html.indexOf("\n");
    int start = 0;
    int line = 1;
    while (idx != -1) {
      withLineNumbers.append(html.substring(start, idx));
      withLineNumbers.append("&para;<span class=\"difflinenumber\">");
      withLineNumbers.append(line++);
      withLineNumbers.append("</span><br/>");
      start = idx;
      idx = html.indexOf("\n", start + 1);
    }
    if (start != html.length())
      withLineNumbers.append(html.substring(start, html.length()));

    StringBuilder result = new StringBuilder();
    for (int i = 1; i < changeCounter; i++)
      result.append("<a href=\"#edit" + i + "\">" + i + "</a>&nbsp;");
    if (changeCounter != 1)
      result.append("<br/><br/>");
    result.append(withLineNumbers);
    return result.toString();
  }

  protected String getBookmark(int changeCounter) {
    return "<a name=\"edit" + changeCounter + "\">";
  }
}
