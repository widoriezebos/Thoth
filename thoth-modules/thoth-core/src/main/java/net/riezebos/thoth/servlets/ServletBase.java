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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.commands.CommandOperation;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.context.ContextManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextManagerException;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.util.ThothUtil;

public abstract class ServletBase extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(ServletBase.class);
  private ThothEnvironment thothEnvironment = null;

  protected abstract void handleRequest(HttpServletRequest request, HttpServletResponse response, CommandOperation operation)
      throws ServletException, IOException, ContentManagerException;

  abstract protected Identity getCurrentIdentity(HttpServletRequest request);

  public ServletBase() {
  }

  @Override
  public void init() throws ServletException {
    super.init();
    if (thothEnvironment == null)
      thothEnvironment = ThothEnvironment.getSharedThothContext();
  }

  public ThothEnvironment getThothEnvironment() {
    return thothEnvironment;
  }

  public void setThothContext(ThothEnvironment thothEnvironment) {
    this.thothEnvironment = thothEnvironment;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      handleRequest(request, response, CommandOperation.GET);
    } catch (ContentManagerException | IOException e) {
      handleError(request, response, e);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      handleRequest(request, response, CommandOperation.POST);
    } catch (ContentManagerException | IOException e) {
      handleError(request, response, e);
    }
  }

  protected void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) throws ServletException, IOException {
    LOG.error(e.getMessage(), e);
  }

  protected String getTitle(HttpServletRequest request) {
    String path = getPath(request);
    path = ThothUtil.getPartAfterLast(path, "/");
    path = ThothUtil.getPartBeforeLast(path, ".");
    path = ThothUtil.stripPrefix(path, "/");
    return path;
  }

  protected String getPath(HttpServletRequest request) {
    String path = getRequestPath(request);
    path = ThothUtil.stripPrefix(path, "/");

    // Context only? Then path is empty
    if (path.indexOf("/") == -1)
      path = "";
    else
      path = ThothUtil.getPartAfterFirst(path, "/");

    return path;
  }

  protected String getContextUrl(HttpServletRequest request) {
    return request.getContextPath() + "/" + getContext(request);
  }

  // Handle some differences between servlet mappings like '/' and '/*'
  // and end any confusion right here
  protected String getRequestPath(HttpServletRequest request) {
    String servletPath = request.getServletPath();
    String pathInfo = request.getPathInfo();
    return StringUtils.isBlank(pathInfo) ? servletPath : pathInfo;
  }

  protected String getContext(HttpServletRequest request) {
    String path = getRequestPath(request);
    path = ThothUtil.stripPrefix(path, "/");
    path = ThothUtil.getPartBeforeFirst(path, "/");
    return path;
  }

  public Skin getSkin(HttpServletRequest request) throws ServletException {
    try {
      Skin skin = null;
      Configuration configuration = getConfiguration();

      String context = getContext(request);
      if (!StringUtils.isBlank(context) && !getContextManager().isValidContext(context))
        return null;

      if (StringUtils.isBlank(context)) {
        context = configuration.getMainIndexSkinContext();
      }

      ContentManager contentManager = getThothEnvironment().getContentManager(context);
      SkinManager skinManager = contentManager.getSkinManager();
      String path = getPath(request);

      // First check for a skin override; i.e. a request with ?skin=name in it
      String skinOverride = request.getParameter("skin");
      if (StringUtils.isNotBlank(skinOverride)) {
        skin = skinManager.getSkinByName(skinOverride);
        if (skin == null)
          LOG.warn("Skin with name = " + skinOverride + " not found. Ignoring override.");
      }

      // No override (or valid) so do normal processing
      if (skin == null)
        skin = skinManager.determineSkin(path);

      return skin;
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  protected Configuration getConfiguration() {
    return getThothEnvironment().getConfiguration();
  }

  protected ContextManager getContextManager() throws ContextManagerException {
    return getThothEnvironment().getContextManager();
  }

  protected Map<String, Object> getParameters(HttpServletRequest request) throws ServletException {
    Map<String, Object> result = new HashMap<>();

    Enumeration<String> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String key = parameterNames.nextElement();
      result.put(key, request.getParameter(key));
    }

    String contextName = getContext(request);
    Skin skin = getSkin(request);
    String skinBase = null;
    if (skin != null) {
      String baseUrl = ThothUtil.prefix(skin.getBaseUrl(), "/");
      if (skin.isFromClassPath()) {
        skinBase = ContentManager.NATIVERESOURCES + baseUrl;
      } else {
        skinBase = baseUrl;
      }
    }
    Configuration configuration = getConfiguration();
    Date now = new Date();
    String path = ThothUtil.prefix(getPath(request), "/");
    result.put(Renderer.CONTEXT_PARAMETER, contextName);
    result.put(Renderer.SKINBASE_PARAMETER, skinBase);
    result.put(Renderer.CONTEXTURL_PARAMETER, getContextUrl(request));
    result.put(Renderer.CONTEXTPATH_PARAMETER, request.getContextPath());
    result.put(Renderer.PATH_PARAMETER, path);
    result.put(Renderer.TITLE_PARAMETER, getTitle(request));
    result.put(Renderer.SKIN, skin == null ? null : skin.getName());
    result.put(Renderer.TODAY, configuration.getDateFormat().format(now));
    result.put(Renderer.NOW, configuration.getTimestampFormat().format(now));
    result.put(Renderer.OUTPUT_FORMATS, configuration.getOutputFormats());
    result.put(Renderer.REFRESH_PARAMETER, getRefreshTimestamp(contextName));

    Set<String> permissions = new HashSet<>();
    result.put(Renderer.LOGGED_IN, isLoggedIn(request));

    Identity identity = getCurrentIdentity(request);
    if (identity != null) {
      result.put(Renderer.IDENTITY, identity.getIdentifier());
      result.put(Renderer.USER_FULL_NAME, identity.getDescription());
      for (Permission permission : identity.getEffectivePermissions())
        permissions.add(String.valueOf(permission));
    } else {
      result.put(Renderer.IDENTITY, null);
    }
    result.put(Renderer.PERMISSIONS, permissions);

    return result;
  }

  protected boolean isLoggedIn(HttpServletRequest request) {
    return getCurrentIdentity(request) instanceof User;
  }

  private String getRefreshTimestamp(String contextName) {
    try {
      Date refreshTimestamp = getThothEnvironment().getRefreshTimestamp(contextName);
      SimpleDateFormat timestampFormat = getConfiguration().getTimestampFormat();
      return refreshTimestamp == null ? "Never" : timestampFormat.format(refreshTimestamp);
    } catch (ContentManagerException e) {
      LOG.error(e.getMessage(), e);
      return "Unknown";
    }
  }

  protected Skin getSkinNoFail(HttpServletRequest request) {
    Skin result;
    try {
      result = getSkin(request);
    } catch (Exception e2) {
      result = new Skin();
    }
    return result;
  }

  protected Map<String, Object> getParametersNoFail(HttpServletRequest request) {
    Map<String, Object> result;
    try {
      result = getParameters(request);
    } catch (Exception e2) {
      result = new HashMap<>();
    }
    return result;
  }

  protected String getContextNoFail(HttpServletRequest request) {
    String context;
    try {
      context = getContext(request);
    } catch (Exception e2) {
      context = null;
    }
    return context;
  }

  protected String getPathNoFail(HttpServletRequest request) {
    String path;
    try {
      path = getPath(request);
    } catch (Exception e2) {
      path = "";
    }
    return path;
  }

}
