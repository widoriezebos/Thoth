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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.comments.Comment;
import net.riezebos.thoth.content.comments.CommentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.markdown.util.ProcessorError;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;

public class ValidationReportCommand extends RendererBase implements Command {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationReportCommand.class);

  public static final String DELETECOMMENT = "deletecomment";

  public ValidationReportCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "validationreport";
  }

  @Override
  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {
    try {
      ContentManager contentManager = getContentManager(contextName);
      if (!contentManager.getAccessManager().hasPermission(identity, path, Permission.VALIDATE))
        return RenderResult.FORBIDDEN;

      RenderResult result;

      String operationCode = (String) arguments.get(OPERATION_ARGUMENT);
      if (StringUtils.isNotBlank(operationCode))
        result = handleOperation(operationCode, identity, contextName, path, operation, arguments, skin, outputStream);
      else
        result = handleRender(identity, contextName, path, operation, arguments, skin, outputStream);

      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  protected RenderResult handleRender(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws ContentManagerException, ServletException, UnsupportedEncodingException, IOException {

    CommentManager commentManager = getThothEnvironment().getCommentManager();
    ContentManager contentManager = getContentManager(contextName);

    List<ProcessorError> errors = contentManager.getValidationErrors();
    List<String> allPaths = contentManager.getAllPaths();
    List<Comment> orphanedComments = commentManager.getOrphanedComments(contextName, allPaths);
    Map<String, Object> variables = new HashMap<>(arguments);

    Map<String, List<ProcessorError>> errorsByDocument = new HashMap<>();
    errors.stream().forEach(pe -> {
      List<ProcessorError> list = errorsByDocument.get(pe.getFile());
      if (list == null) {
        list = new ArrayList<>();
        errorsByDocument.put(pe.getFile(), list);
      }
      list.add(pe);
    });

    errorsByDocument.entrySet().stream().forEach(l -> Collections.sort(l.getValue()));

    List<String> documents = new ArrayList<>(errorsByDocument.keySet());
    Collections.sort(documents);

    variables.put("documents", documents);
    variables.put("errorsByDocument", errorsByDocument);
    variables.put("errors", errors);
    variables.put("orphanedComments", orphanedComments);

    render(skin.getSkinBaseFolder(), skin.getValidationTemplate(), contextName, arguments, variables, outputStream);

    return RenderResult.OK;
  }

  protected RenderResult handleOperation(String operationCode, Identity identity, String contextName, String path, CommandOperation operation,
      Map<String, Object> arguments, Skin skin, OutputStream outputStream)
      throws UnsupportedEncodingException, ContentManagerException, ServletException, IOException {
    if (operationCode != null) {
      switch (operationCode) {
      case DELETECOMMENT:
        deleteComment(identity, contextName, path, operation, arguments, skin, outputStream);
        break;
      default:
        LOG.warn("Unsupported operation code: " + operationCode);
      }
    }
    return handleRender(identity, contextName, path, operation, arguments, skin, outputStream);
  }

  protected void deleteComment(Identity identity, String context, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {

    Map<String, Object> commentArguments = new HashMap<>(arguments);
    commentArguments.put(OPERATION_ARGUMENT, CommentCommand.DELETE);
    commentArguments.put(MODE_ARGUMENT, MODE_SILENT);
    Command commentCommand = (Command) getRendererProvider().getRenderer(CommentCommand.TYPE_CODE);
    commentCommand.execute(identity, context, path, operation, commentArguments, skin, outputStream);
  }
}
