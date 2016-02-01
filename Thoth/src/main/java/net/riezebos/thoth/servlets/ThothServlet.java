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

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.CustomRendererDefinition;
import net.riezebos.thoth.commands.BranchIndexCommand;
import net.riezebos.thoth.commands.BrowseCommand;
import net.riezebos.thoth.commands.Command;
import net.riezebos.thoth.commands.DiffCommand;
import net.riezebos.thoth.commands.IndexCommand;
import net.riezebos.thoth.commands.MetaCommand;
import net.riezebos.thoth.commands.PullCommand;
import net.riezebos.thoth.commands.ReindexCommand;
import net.riezebos.thoth.commands.RevisionsCommand;
import net.riezebos.thoth.commands.SearchCommand;
import net.riezebos.thoth.commands.ValidationReportCommand;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.CustomRenderer;
import net.riezebos.thoth.renderers.HtmlRenderer;
import net.riezebos.thoth.renderers.RawRenderer;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.renderers.Renderer.RenderResult;
import net.riezebos.thoth.util.ThothUtil;

public class ThothServlet extends ServletBase {
  private static final Logger LOG = LoggerFactory.getLogger(ThothServlet.class);

  private static final long serialVersionUID = 1L;

  private Map<String, Renderer> renderers = new HashMap<>();
  private Map<String, Command> commands = new HashMap<>();
  private IndexCommand indexCommand;
  private BranchIndexCommand branchIndexCommand;
  private Renderer defaultRenderer;
  private Set<String> renderedExtensions = new HashSet<>();

  @Override
  public void init() throws ServletException {
    super.init();
    setupCommands();
    setupRenderers();
    List<String> documentExtensions = Configuration.getInstance().getDocumentExtensions();
    renderedExtensions.addAll(documentExtensions);
  }

  protected void setupCommands() {
    indexCommand = new IndexCommand();
    branchIndexCommand = new BranchIndexCommand();

    registerCommand(branchIndexCommand);
    registerCommand(new DiffCommand());
    registerCommand(indexCommand);
    registerCommand(new MetaCommand());
    registerCommand(new PullCommand());
    registerCommand(new ReindexCommand());
    registerCommand(new RevisionsCommand());
    registerCommand(new SearchCommand());
    registerCommand(new ValidationReportCommand());
    registerCommand(new BrowseCommand());
  }

  protected void setupRenderers() {
    defaultRenderer = new HtmlRenderer();
    registerRenderer(defaultRenderer);
    registerRenderer(new RawRenderer());

    // Setup any custom renderers
    List<CustomRendererDefinition> customRendererDefinitions = Configuration.getInstance().getCustomRenderers();
    for (CustomRendererDefinition customRendererDefinition : customRendererDefinitions) {
      CustomRenderer renderer = new CustomRenderer(customRendererDefinition);
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

  protected Renderer getRenderer(String typeCode) {
    Renderer renderer = renderers.get(typeCode);
    if (renderer == null)
      renderer = defaultRenderer;
    return renderer;
  }

  protected Command getCommand(String typeCode) {
    Command command = commands.get(typeCode);
    return command;
  }

  @Override
  protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ContentManagerException {

    if (!handleCommand(request, response)) {

      String branch = getBranch(request);
      String path = getPath(request);
      if (("/" + branch + "/").equalsIgnoreCase(NATIVERESOURCES))
        handleNativeResource(path, request, response);
      else if (StringUtils.isBlank(branch) && StringUtils.isBlank(path))
        executeCommand(indexCommand, request, response);
      else if (StringUtils.isBlank(path))
        executeCommand(branchIndexCommand, request, response);
      else {
        if (!renderDocument(request, response))
          streamResource(request, response);
      }
    }
  }

  protected boolean renderDocument(HttpServletRequest request, HttpServletResponse response) throws RenderException, ServletException, IOException {
    boolean result = false;
    String path = getPath(request);
    String extension = ThothUtil.getExtension(path);
    if (extension != null && renderedExtensions.contains(extension)) {
      long ms = System.currentTimeMillis();
      Renderer renderer = getRenderer(request.getParameter("output"));
      response.setContentType(renderer.getContentType(getParameters(request)));
      RenderResult renderResult = renderer.execute(getBranch(request), getPath(request), getParameters(request), getSkin(request), response.getOutputStream());
      switch (renderResult) {
      case NOT_FOUND:
        LOG.info("404 on request " + request.getRequestURI());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        break;
      case FORBIDDEN:
        LOG.warn("Denied request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        break;
      default:
      }
      LOG.debug("Handled request " + request.getRequestURI() + " in " + (System.currentTimeMillis() - ms) + " ms");
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
    Map<String, Object> parameters = getParameters(request);
    response.setContentType(command.getContentType(parameters));
    command.execute(getBranch(request), getPath(request), parameters, getSkin(request), response.getOutputStream());
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

  protected void streamResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, BranchNotFoundException, IOException {
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

}
