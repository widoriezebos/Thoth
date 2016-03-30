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
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.commands.BrowseCommand;
import net.riezebos.thoth.commands.Command;
import net.riezebos.thoth.commands.CommandOperation;
import net.riezebos.thoth.commands.ContextIndexCommand;
import net.riezebos.thoth.commands.DiffCommand;
import net.riezebos.thoth.commands.ErrorPageCommand;
import net.riezebos.thoth.commands.IndexCommand;
import net.riezebos.thoth.commands.LoginCommand;
import net.riezebos.thoth.commands.LogoutCommand;
import net.riezebos.thoth.commands.ManageContextsCommand;
import net.riezebos.thoth.commands.ManageUsersCommand;
import net.riezebos.thoth.commands.MetaCommand;
import net.riezebos.thoth.commands.PullCommand;
import net.riezebos.thoth.commands.ReindexCommand;
import net.riezebos.thoth.commands.RevisionsCommand;
import net.riezebos.thoth.commands.SearchCommand;
import net.riezebos.thoth.commands.UserProfileCommand;
import net.riezebos.thoth.commands.ValidationReportCommand;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.RendererChangeListener;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.AccessManager;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.renderers.CustomRenderer;
import net.riezebos.thoth.renderers.HtmlRenderer;
import net.riezebos.thoth.renderers.RawRenderer;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;
import net.riezebos.thoth.user.Group;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;
import net.riezebos.thoth.util.MimeTypeUtil;
import net.riezebos.thoth.util.ThothUtil;

public class ThothServlet extends ServletBase implements RendererProvider, RendererChangeListener {
  private static final String SESSION_USER_KEY = "user";

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
    getThothEnvironment().addRendererChangedListener(this);
  }

  @Override
  protected void handleRequest(HttpServletRequest request, HttpServletResponse response, CommandOperation operation)
      throws ServletException, IOException, ContentManagerException {

    try {
      if (!handleCommand(request, response, operation)) {

        String context = getContext(request);
        String path = getPath(request);
        if (("/" + context).equalsIgnoreCase(ContentManager.NATIVERESOURCES))
          streamClassPathResource(path, request, response);
        else if (StringUtils.isBlank(context) && StringUtils.isBlank(path))
          handleMainIndex(request, response, operation);
        else if (StringUtils.isBlank(path))
          executeCommand(contextIndexCommand, request, response, operation);
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

  protected void handleMainIndex(HttpServletRequest request, HttpServletResponse response, CommandOperation operation)
      throws ServletException, IOException, ContentManagerException {
    Map<String, ContextDefinition> contextDefinitions = getContextManager().getContextDefinitions();
    boolean redirected = false;
    if (contextDefinitions.size() == 1) {
      // Before we get smart and redirect to the one and only context; we should check whether we have access
      // Because otherwise we will loose any way to log in

      ContextDefinition oneAndOnly = contextDefinitions.values().iterator().next();
      String contextName = oneAndOnly.getName();
      ContentManager contentManager = getThothEnvironment().getContentManager(contextName);
      boolean hasPermission = contentManager.getAccessManager().hasPermission(getCurrentIdentity(request), "/", Permission.BASIC_ACCESS);
      if (hasPermission) {
        String mainRedirect = ThothUtil.suffix(getRootRedirect(request), "/") + contextName;
        response.sendRedirect(mainRedirect);
        redirected = true;
      }
    }
    if (!redirected)
      executeCommand(indexCommand, request, response, operation);
  }

  @Override
  protected void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) throws ServletException, IOException {
    LOG.error(e.getMessage(), e);
    Command errorPageCommand = getCommand(ErrorPageCommand.COMMAND);
    if (errorPageCommand == null)
      errorPageCommand = new ErrorPageCommand(getThothEnvironment(), this); // Fallback; we should not fail here

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
      errorPageCommand.execute(getCurrentIdentity(request), context, path, CommandOperation.GET, parameters, skin, response.getOutputStream());
    } catch (RenderException e1) {
      // Well if this fails; we leave it up to the container. Let's throw new original exception
      // But we still want to know what failed on the error page; so:
      LOG.error(e1.getMessage(), e1);
      throw new ServletException(e);
    }
  }

  @Override
  public void rendererDefinitionChanged() {
    setupRenderers();
  }

  protected void setupCommands() {
    ThothEnvironment thothEnvironment = getThothEnvironment();
    indexCommand = new IndexCommand(thothEnvironment, this);
    contextIndexCommand = new ContextIndexCommand(thothEnvironment, this);

    registerCommand(contextIndexCommand);
    registerCommand(new DiffCommand(thothEnvironment, this));
    registerCommand(indexCommand);
    registerCommand(new MetaCommand(thothEnvironment, this));
    registerCommand(new LoginCommand(thothEnvironment, this));
    registerCommand(new LogoutCommand(thothEnvironment, this));
    registerCommand(new PullCommand(thothEnvironment, this));
    registerCommand(new ReindexCommand(thothEnvironment, this));
    registerCommand(new RevisionsCommand(thothEnvironment, this));
    registerCommand(new ManageUsersCommand(thothEnvironment, this));
    registerCommand(new ManageContextsCommand(thothEnvironment, this));
    registerCommand(new UserProfileCommand(thothEnvironment, this));
    registerCommand(new SearchCommand(thothEnvironment, this));
    registerCommand(new ValidationReportCommand(thothEnvironment, this));
    registerCommand(new BrowseCommand(thothEnvironment, this));
    registerCommand(new ErrorPageCommand(thothEnvironment, this));
  }

  protected void setupRenderers() {

    Map<String, Renderer> rendererMap = new HashMap<>();

    defaultRenderer = new HtmlRenderer(getThothEnvironment(), this);
    registerRenderer(rendererMap, defaultRenderer);
    registerRenderer(rendererMap, new RawRenderer(getThothEnvironment(), this));

    // Setup any custom renderers
    List<CustomRendererDefinition> customRendererDefinitions = getConfiguration().getCustomRenderers();
    for (CustomRendererDefinition customRendererDefinition : customRendererDefinitions) {
      CustomRenderer renderer = new CustomRenderer(getThothEnvironment(), customRendererDefinition, this);
      renderer.setTypeCode(customRendererDefinition.getExtension());
      renderer.setContentType(customRendererDefinition.getContentType());
      renderer.setCommandLine(customRendererDefinition.getCommandLine());
      registerRenderer(rendererMap, renderer);

      // Override default renderer?
      if (defaultRenderer.getTypeCode().equals(renderer.getTypeCode()))
        defaultRenderer = renderer;
    }
    renderers = rendererMap;
  }

  protected void registerRenderer(Map<String, Renderer> rendererMap, Renderer renderer) {
    rendererMap.put(renderer.getTypeCode().toLowerCase(), renderer);
  }

  protected void registerCommand(Command command) {
    commands.put(command.getTypeCode().toLowerCase(), command);
  }

  @Override
  public Renderer getRenderer(String typeCode) {
    Renderer renderer = null;
    if (typeCode != null) {
      String key = typeCode.toLowerCase();
      renderer = renderers.get(key);
      if (renderer == null)
        renderer = commands.get(key);
    }
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
      RenderResult renderResult = renderer.execute(getCurrentIdentity(request), getContext(request), getPath(request), CommandOperation.GET,
          getParameters(request), getSkin(request), bos);
      String requestURI = request.getRequestURI();
      switch (renderResult.getCode()) {
      case NOT_FOUND:
        LOG.info("404 on request " + requestURI);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        break;
      case FORBIDDEN:
        handleForbidden(request, response);
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

  protected void handleForbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (isLoggedIn(request))
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    else {
      String loginRedirect = getRootRedirect(request);
      loginRedirect += "?cmd=" + LoginCommand.TYPE_CODE;
      response.sendRedirect(loginRedirect);
    }
  }

  protected boolean handleCommand(HttpServletRequest request, HttpServletResponse response, CommandOperation operation)
      throws IOException, ServletException, ContentManagerException {
    Command command = getCommand(request.getParameter("cmd"));
    boolean result = false;
    if (command != null) {
      executeCommand(command, request, response, operation);
      result = true;
    }
    return result;
  }

  protected void executeCommand(Command command, HttpServletRequest request, HttpServletResponse response, CommandOperation operation)
      throws RenderException, ServletException, IOException, ContextManagerException {
    String context = getContext(request);
    if (StringUtils.isBlank(context) || getContextManager().isValidContext(context)) {
      Map<String, Object> parameters = getParameters(request);
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      RenderResult renderResult = command.execute(getCurrentIdentity(request), context, getPath(request), operation, parameters, getSkin(request), bos);

      String rootRedirect = getRootRedirect(request);
      switch (renderResult.getCode()) {
      case OK:
        // Only now will we touch the response; this to avoid sending stuff out already and then
        // encountering an error. This might complicate error handling (rendering an error page)
        // otherwise
        response.setContentType(command.getContentType(parameters));
        IOUtils.copy(new ByteArrayInputStream(bos.toByteArray()), response.getOutputStream());
        break;
      case FORBIDDEN:
        handleForbidden(request, response);
        break;
      case LOGGED_OUT:
        setCurrentUser(request, null);
        request.getSession().invalidate();
        response.sendRedirect(rootRedirect);
        break;
      case LOGGED_IN:
        User user = renderResult.getArgument(LoginCommand.USER_ARGUMENT);
        setCurrentUser(request, user);
        if (StringUtils.isBlank(context))
          response.sendRedirect(rootRedirect);
        else
          response.sendRedirect(getContextUrl(request));
        break;
      default:
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }

    } else
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  private String getRootRedirect(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    if (StringUtils.isBlank(contextPath))
      contextPath = "/";
    return contextPath;
  }

  protected void streamResource(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ContentManagerException {
    long ms = System.currentTimeMillis();

    String path = getPath(request);
    String contextName = getContext(request);
    if (getContextManager().isValidContext(contextName)) {
      ContentManager contentManager = getThothEnvironment().getContentManager(contextName);
      AccessManager accessManager = contentManager.getAccessManager();
      boolean hasPermission = accessManager.hasPermission(getCurrentIdentity(request), path, Permission.READ_RESOURCE);
      if (!hasPermission) {
        handleForbidden(request, response);
      } else {
        InputStream is = contentManager.getInputStream(path);
        if (is != null) {
          setMimeType(getRequestPath(request), response);
          IOUtils.copy(is, response.getOutputStream());
        } else {
          LOG.warn("404 on request " + request.getRequestURI());
          response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
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

  @Override
  public Identity getCurrentIdentity(HttpServletRequest request) {

    String ssoToken = request.getParameter(UserManager.SSO_TOKEN_NAME);
    if (ssoToken != null) {
      try {
        Identity identity = getThothEnvironment().getUserManager().getIdentityForToken(ssoToken);
        if (identity != null) {
          // We will start a new session here; but we mark it for a short life since this
          // is meant for a single render request only.
          HttpSession session = request.getSession(true);
          session.setAttribute(SESSION_USER_KEY, identity);
          session.setMaxInactiveInterval(30);
          return identity;
        }
      } catch (ContentManagerException e) {
        LOG.error(e.getMessage());
      }
    }

    HttpSession session = request.getSession(false);
    Identity result = null;
    if (session != null)
      result = (Identity) session.getAttribute(SESSION_USER_KEY);
    if (result == null)
      result = getDefaultGroup();
    return result;
  }

  public void setCurrentUser(HttpServletRequest request, User user) {
    // Invalidate any current session before we start a fresh one with a logged in user
    HttpSession session = request.getSession(false);
    if (session != null)
      session.invalidate();
    session = request.getSession(true);
    session.setAttribute(SESSION_USER_KEY, user);
  }

  protected Group getDefaultGroup() {
    try {
      UserManager userManager = getThothEnvironment().getUserManager();
      String defaultGroup = getConfiguration().getDefaultGroup();
      Group group = userManager.getGroup(defaultGroup);
      if (group == null)
        LOG.warn("Default group " + defaultGroup + " is not defined.");
      return group;
    } catch (UserManagerException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }

}
