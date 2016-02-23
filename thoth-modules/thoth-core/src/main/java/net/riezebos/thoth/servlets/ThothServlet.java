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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.commands.BrowseCommand;
import net.riezebos.thoth.commands.Command;
import net.riezebos.thoth.commands.ContextIndexCommand;
import net.riezebos.thoth.commands.DiffCommand;
import net.riezebos.thoth.commands.ErrorPageCommand;
import net.riezebos.thoth.commands.IndexCommand;
import net.riezebos.thoth.commands.MetaCommand;
import net.riezebos.thoth.commands.PullCommand;
import net.riezebos.thoth.commands.ReindexCommand;
import net.riezebos.thoth.commands.RevisionsCommand;
import net.riezebos.thoth.commands.SearchCommand;
import net.riezebos.thoth.commands.ValidationReportCommand;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.CustomRenderer;
import net.riezebos.thoth.renderers.HtmlRenderer;
import net.riezebos.thoth.renderers.RawRenderer;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.renderers.Renderer.RenderResult;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;
import net.riezebos.thoth.util.MimeTypeUtil;
import net.riezebos.thoth.util.ThothUtil;

public class ThothServlet extends ServletBase implements RendererProvider {
  private static final Logger LOG = LoggerFactory.getLogger(ThothServlet.class);

  private static final long serialVersionUID = 1L;

  private Map<String, Renderer> renderers = new HashMap<>();
  private Map<String, Command> commands = new HashMap<>();
  private IndexCommand indexCommand;
  private ContextIndexCommand contextIndexCommand;
  private Renderer defaultRenderer;
  private Set<String> renderedExtensions = new HashSet<>();

  @Override
  public void init() throws ServletException {
    super.init();
    setupCommands();
    setupRenderers();
    List<String> documentExtensions = getConfiguration().getDocumentExtensions();
    renderedExtensions.addAll(documentExtensions);
  }

  @Override
  protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ContentManagerException {

    try {
      if (!handleCommand(request, response)) {

        String context = getContext(request);
        String path = getPath(request);
        if (("/" + context).equalsIgnoreCase(ContentManager.NATIVERESOURCES))
          streamClassPathResource(path, request, response);
        else if (StringUtils.isBlank(context) && StringUtils.isBlank(path))
          executeCommand(indexCommand, request, response);
        else if (StringUtils.isBlank(path))
          executeCommand(contextIndexCommand, request, response);
        else {
          if (!renderDocument(request, response))
            streamResource(request, response);
        }
      }
    } catch (ContextNotFoundException e) {
      LOG.info("404 on request " + request.getRequestURI());
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  @Override
  protected void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) throws ServletException, IOException {
    Command errorPageCommand = getCommand(ErrorPageCommand.COMMAND);
    if (errorPageCommand == null)
      errorPageCommand = new ErrorPageCommand(getThothContext()); // Fallback; we should not fail here

    String context = getContextNoFail(request);
    String path = getPathNoFail(request);
    Skin skin = getSkinNoFail(request);
    Map<String, Object> parameters = getParametersNoFail(request);
    parameters.put("message", e.getMessage());
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      e.printStackTrace(new PrintWriter(bos, true));
      parameters.put("stack", new String(bos.toByteArray(), "UTF-8"));
    }
    try {
      response.setContentType(errorPageCommand.getContentType(getParameters(request)));
      errorPageCommand.execute(context, path, parameters, skin, response.getOutputStream());
    } catch (RenderException e1) {
      // Well if this fails; we leave it up to the container. Let's throw new original exception
      // But we still want to know what failed on the error page; so:
      LOG.error(e1.getMessage(), e1);
      throw new ServletException(e);
    }
  }

  protected void setupCommands() {
    ThothEnvironment thothEnvironment = getThothContext();
    indexCommand = new IndexCommand(thothEnvironment);
    contextIndexCommand = new ContextIndexCommand(thothEnvironment);

    registerCommand(contextIndexCommand);
    registerCommand(new DiffCommand(thothEnvironment));
    registerCommand(indexCommand);
    registerCommand(new MetaCommand(thothEnvironment));
    registerCommand(new PullCommand(thothEnvironment));
    registerCommand(new ReindexCommand(thothEnvironment));
    registerCommand(new RevisionsCommand(thothEnvironment));
    registerCommand(new SearchCommand(thothEnvironment));
    registerCommand(new ValidationReportCommand(thothEnvironment));
    registerCommand(new BrowseCommand(thothEnvironment));
    registerCommand(new ErrorPageCommand(thothEnvironment));
  }

  protected void setupRenderers() {
    defaultRenderer = new HtmlRenderer(getThothContext());
    registerRenderer(defaultRenderer);
    registerRenderer(new RawRenderer(getThothContext()));

    // Setup any custom renderers
    List<CustomRendererDefinition> customRendererDefinitions = getConfiguration().getCustomRenderers();
    for (CustomRendererDefinition customRendererDefinition : customRendererDefinitions) {
      CustomRenderer renderer = new CustomRenderer(getThothContext(), customRendererDefinition, this);
      renderer.setTypeCode(customRendererDefinition.getExtension());
      renderer.setContentType(customRendererDefinition.getContentType());
      renderer.setCommandLine(customRendererDefinition.getCommandLine());
      registerRenderer(renderer);

      // Override default renderer?
      if (defaultRenderer.getTypeCode().equals(renderer.getTypeCode()))
        defaultRenderer = renderer;
    }
  }

  protected void registerRenderer(Renderer renderer) {
    renderers.put(renderer.getTypeCode().toLowerCase(), renderer);
  }

  protected void registerCommand(Command command) {
    commands.put(command.getTypeCode().toLowerCase(), command);
  }

  public Renderer getRenderer(String typeCode) {
    Renderer renderer = null;
    if (typeCode != null)
      renderer = renderers.get(typeCode.toLowerCase());
    if (renderer == null)
      renderer = defaultRenderer;
    return renderer;
  }

  protected Command getCommand(String typeCode) {
    Command command = commands.get(typeCode);
    return command;
  }

  protected boolean renderDocument(HttpServletRequest request, HttpServletResponse response) throws RenderException, ServletException, IOException {
    boolean result = false;
    String path = getPath(request);
    String extension = ThothUtil.getExtension(path);
    if (extension != null && renderedExtensions.contains(extension)) {
      long ms = System.currentTimeMillis();
      Renderer renderer = getRenderer(request.getParameter("output"));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      RenderResult renderResult = renderer.execute(getContext(request), getPath(request), getParameters(request), getSkin(request), bos);
      String requestURI = request.getRequestURI();
      switch (renderResult) {
      case NOT_FOUND:
        LOG.info("404 on request " + requestURI);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        break;
      case FORBIDDEN:
        LOG.warn("Denied request " + requestURI + " in " + (System.currentTimeMillis() - ms) + " ms");
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        break;
      default:
        // Only now will we touch the response; this to avoid sending stuff out already and then
        // encountering an error. This might complicate error handling (rendering an error page)
        // otherwise
        response.setContentType(renderer.getContentType(getParameters(request)));

        String fileName = ThothUtil.getNameOnly(path) + "." + renderer.getTypeCode();
        response.setHeader("Content-Disposition", "filename=" + fileName);

        IOUtils.copy(new ByteArrayInputStream(bos.toByteArray()), response.getOutputStream());
      }
      LOG.debug("Handled request " + requestURI + " in " + (System.currentTimeMillis() - ms) + " ms");
      result = true;
    }
    return result;
  }

  protected boolean handleCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, ContentManagerException {
    Command command = getCommand(request.getParameter("cmd"));
    boolean result = false;
    if (command != null) {
      executeCommand(command, request, response);
      result = true;
    }
    return result;
  }

  protected void executeCommand(Command command, HttpServletRequest request, HttpServletResponse response)
      throws RenderException, ServletException, IOException {
    String context = getContext(request);
    if (StringUtils.isBlank(context) || getConfiguration().isValidContext(context)) {
      Map<String, Object> parameters = getParameters(request);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      command.execute(context, getPath(request), parameters, getSkin(request), bos);
      // Only now will we touch the response; this to avoid sending stuff out already and then
      // encountering an error. This might complicate error handling (rendering an error page)
      // otherwise
      response.setContentType(command.getContentType(parameters));
      IOUtils.copy(new ByteArrayInputStream(bos.toByteArray()), response.getOutputStream());
    } else
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  protected void streamResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ContentManagerException {
    long ms = System.currentTimeMillis();

    String path = getPath(request);
    String contextName = getContext(request);
    if (getConfiguration().isValidContext(contextName)) {
      ContentManager contentManager = getThothContext().getContentManager(contextName);

      InputStream is = contentManager.getInputStream(path);
      if (is != null) {
        setMimeType(getRequestPath(request), response);
        IOUtils.copy(is, response.getOutputStream());
      } else {
        LOG.warn("404 on request " + request.getRequestURI());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    } else {
      LOG.warn("404 on context of request " + request.getRequestURI());
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    LOG.debug("Handled request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");

  }

  protected void streamClassPathResource(String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
    // We do not want to expose the entire server; so we require the path to start with REQUIRED_PREFIX (which is 'net/riezebos/thoth/skins/')
    if (!path.startsWith(Configuration.REQUIRED_PREFIX))
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    else {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
      if (is == null) {
        LOG.warn("404 on request for native resource " + request.getRequestURI());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      } else {
        setMimeType(getRequestPath(request), response);

        IOUtils.copy(is, response.getOutputStream());
      }
    }
  }

  protected void setMimeType(String path, HttpServletResponse response) {
    String mimeType = MimeTypeUtil.getMimeType(ThothUtil.getExtension(path));
    if (mimeType != null)
      response.setContentType(mimeType);
  }
}
