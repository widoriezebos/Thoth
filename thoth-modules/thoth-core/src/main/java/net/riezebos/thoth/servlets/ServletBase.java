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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.CacheManager;
import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.content.skinning.SkinMapping;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.util.ThothUtil;

public abstract class ServletBase extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(ServletBase.class);

  private Skin defaultSkin = null;
  private SkinManager skinManager = null;

  protected abstract void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ContentManagerException;

  public ServletBase() {
    Configuration.getInstance().validate();
  }

  @Override
  public void init() throws ServletException {
    super.init();
    getContentManager();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      handleRequest(request, response);
    } catch (ContextNotFoundException e) {
      LOG.info("404 on context of " + request.getRequestURI());
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } catch (ContentManagerException | IOException e) {
      handleError(request, response, e);
    }
  }

  protected void handleError(HttpServletRequest request, HttpServletResponse response, Exception e) throws ServletException, IOException {
    LOG.error(e.getMessage(), e);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  protected MarkDownDocument getMarkdown(HttpServletRequest request)
      throws ContextNotFoundException, FileNotFoundException, ContentManagerException, IOException, ServletException {
    return getMarkdown(request, null);
  }

  protected MarkDownDocument getMarkdown(HttpServletRequest request, String suffix)
      throws ContextNotFoundException, ContentManagerException, FileNotFoundException, IOException, ServletException {
    String relativePath = ThothUtil.stripSuffix(getPath(request), suffix);
    String context = getContext(request);

    return getContentManager().getMarkDownDocument(context, relativePath);
  }

  /**
   * Just a convenience wrapper
   *
   * @return
   * @throws ServletException
   */
  protected ContentManager getContentManager() throws ServletException {
    try {
      return ContentManagerFactory.getContentManager();
    } catch (ContentManagerException e) {
      throw new ServletException(e);
    }
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

  protected String prefixWithSlash(String path) {
    if (!StringUtils.isBlank(path) && !path.startsWith("/"))
      path = "/" + path;
    return path;
  }

  protected String getRefreshTimestamp(ContentManager contentManager) throws ServletException {
    Configuration configuration = Configuration.getInstance();
    SimpleDateFormat dateFormat = configuration.getDateFormat();
    Date latestRefresh = getContentManager().getLatestRefresh(null);
    String refresh = latestRefresh == null ? "Never" : dateFormat.format(contentManager.getLatestRefresh(null));
    return refresh;
  }

  public Skin getSkin(HttpServletRequest request) throws ServletException {
    try {
      Skin skin = null;
      Configuration configuration = Configuration.getInstance();

      String context = getContext(request);
      if (StringUtils.isBlank(context)) {
        context = configuration.getGlobalSkinContext();
      }

      CacheManager cacheManager = CacheManager.getInstance(context);
      List<SkinMapping> skinMappings = cacheManager.getSkinMappings();
      if (skinMappings == null) {
        SkinManager skinManager = new SkinManager();
        skinMappings = skinManager.setupSkins(configuration, context, cacheManager);
        defaultSkin = skinManager.getDefaultSkin();
      }

      String path = getPath(request);

      // First check for a skin override; i.e. a request with ?skin=name in it
      String skinOverride = request.getParameter("skin");
      if (StringUtils.isNotBlank(skinOverride)) {
        skin = cacheManager.getSkinByName(skinOverride);
        if (skin == null)
          LOG.warn("Skin with name = " + skinOverride + " not found. Ignoring override.");
      }

      // No override (or valid) so do normal processing
      if (skin == null) {
        for (SkinMapping mapping : skinMappings)
          if (mapping.getPattern().matcher(path).matches()) {
            skin = mapping.getSkin();
            break;
          }
        if (skin == null)
          skin = defaultSkin;
      }
      return skin;
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  protected Map<String, Object> getParameters(HttpServletRequest request) throws ServletException {
    Map<String, Object> result = new HashMap<>();

    Enumeration<String> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String key = parameterNames.nextElement();
      result.put(key, request.getParameter(key));
    }

    String context = getContext(request);
    Skin skin = getSkin(request);
    String skinBase;
    String baseUrl = skin.getBaseUrl();
    if (skin.isFromClassPath()) {
      skinBase = ContentManager.NATIVERESOURCES + baseUrl;
    } else {
      skinBase = prefixWithSlash(request.getContextPath() + baseUrl);
    }

    String path = getPath(request);
    path = prefixWithSlash(path);
    result.put(Renderer.BRANCH_PARAMETER, context);
    result.put(Renderer.SKINBASE_PARAMETER, skinBase);
    result.put(Renderer.BRANCHURL_PARAMETER, getContextUrl(request));
    result.put(Renderer.CONTEXTPATH_PARAMETER, request.getContextPath());
    result.put(Renderer.PATH_PARAMETER, path);
    result.put(Renderer.TITLE_PARAMETER, getTitle(request));
    result.put(Renderer.REFRESH_PARAMETER, getRefreshTimestamp(getContentManager()));
    return result;
  }

  protected SkinManager getSkinManager() {
    if (this.skinManager == null)
      this.skinManager = new SkinManager();
    return this.skinManager;
  }

  protected Skin getSkinNoFail(HttpServletRequest request) {
    Skin result;
    try {
      result = getSkin(request);
    } catch (Exception e2) {
      result = getSkinManager().getDefaultSkin();
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
