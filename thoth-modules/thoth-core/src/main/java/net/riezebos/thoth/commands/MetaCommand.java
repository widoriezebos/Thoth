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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.ConfigurationFactory;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.search.Indexer;
import net.riezebos.thoth.content.search.SearchFactory;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.CommitComparator;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.markdown.critics.CriticProcessingMode;
import net.riezebos.thoth.markdown.util.DocumentNode;
import net.riezebos.thoth.renderers.RendererBase;

public class MetaCommand extends RendererBase implements Command {

  @Override
  public String getTypeCode() {
    return "meta";
  }

  public RenderResult execute(String context, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {

    try {
      RenderResult result = RenderResult.OK;
      String absolutePath = getFileSystemPath(context, path);
      if (absolutePath == null) {
        result = RenderResult.FORBIDDEN;
      } else {
        ContentManager contentManager = getContentManager(context);

        MarkDownDocument markDownDocument = getMarkDownDocument(context, path, true, CriticProcessingMode.DO_NOTHING);

        DocumentNode root = markDownDocument.getDocumentStructure();
        List<DocumentNode> documentNodes = root.flatten(true);
        int pageSize = ConfigurationFactory.getConfiguration().getFileMaxRevisions();

        Map<String, List<Commit>> commitMap = new HashMap<>();
        List<Commit> commitList = new ArrayList<>();
        for (DocumentNode node : documentNodes) {
          List<Commit> latestCommits = contentManager.getCommits(node.getPath(), 1, pageSize).getList();
          commitMap.put(node.getPath(), latestCommits);
          commitList.addAll(latestCommits);
        }
        Collections.sort(commitList, new CommitComparator());

        SearchFactory searchFactory = SearchFactory.getInstance();
        Indexer indexer = searchFactory.getIndexer(context);
        Map<String, List<String>> reverseIndex = indexer.getReverseIndex(context, false);
        Map<String, List<String>> reverseIndexIndirect = indexer.getReverseIndex(context, true);
        List<String> usedBy = reverseIndex.get("/" + path);
        List<String> usedByIndirect = reverseIndexIndirect.get("/" + path);
        Map<String, String> metatags = markDownDocument.getMetatags();
        List<String> metaTagKeys = new ArrayList<>(metatags.keySet());
        Collections.sort(metaTagKeys);

        Map<String, Object> variables = new HashMap<>(arguments);
        variables.put("document", markDownDocument);
        variables.put("usedBy", usedBy);
        variables.put("usedByIndirect", usedByIndirect);
        variables.put("documentNodes", documentNodes);
        variables.put("commitMap", commitMap);
        variables.put("commitList", commitList);
        variables.put("metatagKeys", metaTagKeys);
        variables.put("metatags", metatags);
        variables.put("errors", markDownDocument.getErrors());

        if (asJson(arguments))
          executeJson(variables, outputStream);
        else {
          String metaInformationTemplate = skin.getMetaInformationTemplate();
          renderTemplate(metaInformationTemplate, context, variables, outputStream);
        }
      }
      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
