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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.parboiled.Parboiled;
import org.pegdown.LinkRenderer;
import org.pegdown.Parser;
import org.pegdown.RelaxedParser;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;

import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.comments.Comment;
import net.riezebos.thoth.content.comments.CommentManager;
import net.riezebos.thoth.content.sections.Section;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.RendererBase;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.renderers.util.CustomHtmlSerializer;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.util.ThothUtil;

public class CommentCommand extends RendererBase implements Command {

  public static final String OPERATION_ARGUMENT = "operation";
  public static final String CREATE = "create";
  public static final String COPY = "copy";
  public static final String DELETE = "delete";
  public static final String DOCPATH_ARGUMENT = "docpath";
  public static final String COMMENTTEXT_ARGUMENT = "commenttext";
  public static final String COMMENTID_ARGUMENT = "commentid";
  public static final String EDIT_TEXT = "editText";

  private static final String MARKER = "@@@";
  private static final String DETAILSTART = MARKER + "detailstart" + MARKER;
  private static final String DETAILEND = MARKER + "detailend" + MARKER;

  public CommentCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  public String getTypeCode() {
    return "comment";
  }

  @Override
  public RenderResult execute(Identity identity, String contextName, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream) throws RenderException {

    try {
      RenderResult result = RenderResult.OK;
      ContentManager contentManager = getContentManager(contextName);
      if (!contentManager.getAccessManager().hasPermission(identity, path, Permission.META))
        return RenderResult.FORBIDDEN;

      String operationCode = (String) arguments.get(OPERATION_ARGUMENT);
      if (StringUtils.isNotBlank(operationCode))
        result = handleOperation(operationCode, identity, contextName, path, arguments, skin, outputStream, contentManager);
      else
        result = handleRender(identity, contextName, path, arguments, skin, outputStream, contentManager);

      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  protected RenderResult handleRender(Identity identity, String contextName, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream,
      ContentManager contentManager) throws IOException, ContextNotFoundException, ContentManagerException, ServletException, UnsupportedEncodingException {
    MarkDownDocument markDownDocument = contentManager.getMarkDownDocument(path, suppressErrors(arguments), getCriticProcessingMode(arguments));
    String markdownSource = annotateSections(markDownDocument.getMarkdown());

    Configuration configuration = getConfiguration();
    int extensions = configuration.getMarkdownOptions();
    long parseTimeOut = configuration.getParseTimeOut();
    RelaxedParser parser = Parboiled.createParser(RelaxedParser.class, extensions, parseTimeOut, Parser.DefaultParseRunnerProvider, PegDownPlugins.NONE);
    RootNode ast = parser.parse(ThothUtil.wrapWithNewLines(markdownSource.toCharArray()));

    CustomHtmlSerializer serializer = new CustomHtmlSerializer(new LinkRenderer());
    String body = serializer.toHtml(ast);

    Section section = parseSections(body, contextName, markDownDocument.getPath());

    Map<String, String> metatags = markDownDocument.getMetatags();
    List<String> metaTagKeys = new ArrayList<>(metatags.keySet());
    Collections.sort(metaTagKeys);

    Map<String, Object> variables = new HashMap<>(arguments);
    variables.put("document", markDownDocument);
    variables.put("metatagKeys", metaTagKeys);
    variables.put("metatags", metatags);
    variables.put("mainsection", section);

    if (asJson(arguments))
      executeJson(variables, outputStream);
    else {
      String metaInformationTemplate = skin.getCommentTemplate();
      renderTemplate(metaInformationTemplate, contextName, variables, outputStream);
    }
    return RenderResult.OK;
  }

  protected RenderResult handleOperation(String operationCode, Identity identity, String contextName, String path, Map<String, Object> arguments, Skin skin,
      OutputStream outputStream, ContentManager contentManager)
      throws ContextNotFoundException, UnsupportedEncodingException, IOException, ContentManagerException, ServletException {

    RenderResult result = RenderResult.OK;

    switch (operationCode) {
    case CREATE:
      result = createComment(identity, contextName, path, arguments, contentManager);
      break;
    case DELETE:
      result = deleteComment(identity, path, arguments, contentManager);
      break;
    case COPY:
      result = copyComment(identity, path, arguments, contentManager);
      break;
    default:
      break;
    }

    if (!result.equals(RenderResult.OK))
      return result;
    else
      return handleRender(identity, contextName, path, arguments, skin, outputStream, contentManager);
  }

  protected RenderResult createComment(Identity identity, String contextName, String path, Map<String, Object> arguments, ContentManager contentManager)
      throws ContentManagerException {
    String text = (String) arguments.get(COMMENTTEXT_ARGUMENT);
    String docpath = (String) arguments.get(DOCPATH_ARGUMENT);
    if (!StringUtils.isBlank(text) && !StringUtils.isBlank(docpath)) {
      CommentManager commentManager = getThothEnvironment().getCommentManager();
      Comment comment = new Comment();
      comment.setContextName(contextName);
      comment.setDocumentPath(docpath);
      comment.setUserName(identity.getIdentifier());
      comment.setBody(text);
      commentManager.createComment(comment);
    }
    return RenderResult.OK;
  }

  protected RenderResult deleteComment(Identity identity, String path, Map<String, Object> arguments, ContentManager contentManager)
      throws ContentManagerException {

    String text = (String) arguments.get(COMMENTID_ARGUMENT);
    if (!StringUtils.isBlank(text)) {
      long id = Long.parseLong(text);
      CommentManager commentManager = getThothEnvironment().getCommentManager();
      Comment comment = commentManager.getComment(id);
      if (comment != null) {
        boolean isCreator = comment != null && comment.getUserName().equals(identity.getIdentifier());

        if (isCreator && contentManager.getAccessManager().hasPermission(identity, path, Permission.DELETE_ANY_COMMENT))
          commentManager.deleteComment(comment);
        else
          return RenderResult.FORBIDDEN;
      }
    }
    return RenderResult.OK;
  }

  protected RenderResult copyComment(Identity identity, String path, Map<String, Object> arguments, ContentManager contentManager)
      throws ContentManagerException {

    String text = (String) arguments.get(COMMENTID_ARGUMENT);
    if (!StringUtils.isBlank(text)) {
      long id = Long.parseLong(text);
      CommentManager commentManager = getThothEnvironment().getCommentManager();
      Comment comment = commentManager.getComment(id);
      boolean isCreator = comment != null && comment.getUserName().equals(identity.getIdentifier());

      if (isCreator && contentManager.getAccessManager().hasPermission(identity, path, Permission.EDIT_ANY_COMMENT)) {
        String body = comment.getBody();
        arguments.put(EDIT_TEXT, body);
      } else
        return RenderResult.FORBIDDEN;
    }
    return RenderResult.OK;
  }

  protected Section parseSections(String body, String contextName, String fileName) throws ContentManagerException {
    CommentManager commentManager = getThothEnvironment().getCommentManager();
    Pattern sectionStartPattern = Pattern.compile(DETAILSTART + "(.*?)" + MARKER);
    Pattern sectionEndPattern = Pattern.compile(DETAILEND);

    Stack<Section> sections = new Stack<>();
    Section main = new Section(fileName);
    main.setComments(commentManager.getComments(contextName, fileName, null));
    sections.push(main);

    for (String line : body.split("\n")) {
      Matcher matcher = sectionStartPattern.matcher(line);
      if (matcher.find()) {
        String path = matcher.group(1);
        Section subSection = new Section(path);
        List<Comment> comments = commentManager.getComments(contextName, path, null);
        subSection.setComments(comments);
        sections.peek().addSection(subSection);
        sections.push(subSection);
      } else if (sectionEndPattern.matcher(line).find()) {
        sections.pop();
      } else
        sections.peek().addSection(line);
    }

    return main;
  }

  protected String annotateSections(String markdown) {
    String result = markdown.replaceAll("\\[//\\]\\: \\# \"Include begin\\: ([^\"]*)\"", DETAILSTART + "$1" + MARKER + "\n");
    result = result.replaceAll("\\[//\\]\\: \\# \"Include end\\: .*?\"", DETAILEND + "\n");
    return result;
  }

}
