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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.beans.BookClassification;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.AccessManager;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.util.Classifier;

public class ContextIndexCommand extends RendererBase implements Command {
  public static final String TYPE = "contextindex";

  public ContextIndexCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return TYPE;
  }

  @Override
  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {
      ContentManager contentManager = getContentManager(contextName);
      AccessManager accessManager = contentManager.getAccessManager();
      if (!accessManager.hasPermission(identity, path, Permission.BASIC_ACCESS))
        return RenderResult.FORBIDDEN;

      Classifier classifier = new Classifier();

      List<Book> books = contentManager.getBooks().stream().filter(p -> accessManager.hasPermission(identity, p.getPath(), Permission.BASIC_ACCESS))
          .collect(Collectors.toList());

      Configuration configuration = getThothEnvironment().getConfiguration();
      List<String> classificationNames = configuration.getContextIndexClassifications();
      classificationNames.add("folder");

      // The order is important: to not allow a name clash to overwrite built in variables
      Map<String, Object> variables = new HashMap<>();
      for (String classificationName : classificationNames) {
        List<BookClassification> classifications = classifier.getClassifications(books, classificationName, "Unclassified");
        variables.put("classification_" + classificationName, classifications);
      }
      variables.putAll(arguments);
      variables.put("books", books);
      variables.put("versioncontrolled", contentManager.supportsVersionControl());
      variables.put("classifications", classificationNames);

      render(skin.getSkinBaseFolder(), skin.getContextIndexTemplate(), contextName, arguments, variables, outputStream);

      return RenderResult.OK;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
