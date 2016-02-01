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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import net.riezebos.thoth.content.Skin;
import net.riezebos.thoth.content.SkinMapping;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.ThothUtil;

public abstract class ServletBase extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(ServletBase.class);

  private static final String SKINS_PROPERTIES = "skins.properties";
  protected static final String NATIVERESOURCES = "/nativeresources/";
  private Skin defaultSkin = null;

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
    } catch (BranchNotFoundException e) {
      LOG.info("404 on branch of " + request.getRequestURI());
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } catch (ContentManagerException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  protected MarkDownDocument getMarkdown(HttpServletRequest request)
      throws BranchNotFoundException, FileNotFoundException, ContentManagerException, IOException, ServletException {
    return getMarkdown(request, null);
  }

  protected MarkDownDocument getMarkdown(HttpServletRequest request, String suffix)
      throws BranchNotFoundException, ContentManagerException, FileNotFoundException, IOException, ServletException {
    String relativePath = ThothUtil.stripSuffix(getPath(request), suffix);
    String branch = getBranch(request);

    return getContentManager().getMarkDownDocument(branch, relativePath);
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

  protected String getFileSystemPath(HttpServletRequest request) throws ServletException, BranchNotFoundException, IOException {
    String path = getPath(request);
    String branch = getBranch(request);
    return getContentManager().getFileSystemPath(branch, path);
  }

  protected String getTitle(HttpServletRequest request) {
    String path = getPath(request);
    path = ThothUtil.getPartAfterLast(path, "/");
    path = ThothUtil.getPartBeforeLast(path, ".");
    path = ThothUtil.stripPrefix(path, "/");
    return path;
  }

  protected String getPath(HttpServletRequest request) {
    String path = request.getServletPath();
    path = ThothUtil.stripPrefix(path, "/");

    // Branch only? Then path is empty
    if (path.indexOf("/") == -1)
      path = "";
    else
      path = ThothUtil.getPartAfterFirst(path, "/");

    return path;
  }

  protected String getBranchUrl(HttpServletRequest request) {
    return request.getContextPath() + "/" + getBranch(request);
  }

  protected String getBranch(HttpServletRequest request) {
    String path = request.getServletPath();
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
    String refresh = getContentManager().getLatestRefresh() == null ? "Never" : dateFormat.format(contentManager.getLatestRefresh());
    return refresh;
  }

  public Skin getSkin(HttpServletRequest request) throws ServletException {
    try {
      Skin skin = null;
      Configuration configuration = Configuration.getInstance();

      String branch = getBranch(request);
      if (StringUtils.isBlank(branch)) {
        branch = configuration.getGlobalSkinBranch();
      }

      CacheManager cacheManager = CacheManager.getInstance(branch);
      List<SkinMapping> skinMappings = cacheManager.getSkinMappings();
      if (skinMappings == null) {
        defaultSkin = new Skin(branch, configuration.getDefaultSkin());
        skinMappings = new ArrayList<>();

        String branchFolder = ContentManagerFactory.getContentManager().getBranchFolder(branch);
        String skinMappingFileName = branchFolder + SKINS_PROPERTIES;
        File skinMappingFile = new File(skinMappingFileName);
        if (!skinMappingFile.isFile()) {
          LOG.warn("No " + SKINS_PROPERTIES + " properties file found at " + skinMappingFileName + " so falling back to built in which is "
              + configuration.getDefaultSkin());
          skinMappings.add(new SkinMapping(Pattern.compile(".*"), defaultSkin));
        } else {
          skinMappings.addAll(createSkinMappingsFromFile(branch, skinMappingFileName));
        }
        cacheManager.registerSkinMappings(skinMappings);
      }

      String path = getPath(request);

      for (SkinMapping mapping : skinMappings)
        if (mapping.getPattern().matcher(path).matches())
          skin = mapping.getSkin();
      if (skin == null)
        skin = defaultSkin;
      return skin;
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  protected List<SkinMapping> createSkinMappingsFromFile(String branch, String skinMappingFileName)
      throws FileNotFoundException, IOException, BranchNotFoundException, ContentManagerException, UnsupportedEncodingException {
    List<SkinMapping> skinMappings = new ArrayList<>();

    InputStream is = new FileInputStream(skinMappingFileName);
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
      String line = br.readLine();
      while (line != null) {
        int idx = line.indexOf('=');
        if (!line.startsWith("#") && idx != -1) {
          String patternSpec = line.substring(0, idx).trim();
          String skinFileName = line.substring(idx + 1).trim();
          if (skinFileName.startsWith("/"))
            skinFileName = skinFileName.substring(1);
          Pattern pattern = Pattern.compile(ThothUtil.fileSpec2regExp(patternSpec));
          skinMappings.add(new SkinMapping(pattern, new Skin(branch, skinFileName)));
        }
        line = br.readLine();
      }
    }
    return skinMappings;
  }

  protected Map<String, Object> getParameters(HttpServletRequest request) throws ServletException {
    Map<String, Object> result = new HashMap<>();

    Enumeration<String> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String key = parameterNames.nextElement();
      result.put(key, request.getParameter(key));
    }

    String branch = getBranch(request);
    Skin skin = getSkin(request);
    String skinBase;
    String baseUrl = skin.getBaseUrl();
    if (skin.isFromClassPath()) {
      skinBase = NATIVERESOURCES + baseUrl;
    } else {
      skinBase = prefixWithSlash(request.getContextPath() + baseUrl);
    }

    String path = getPath(request);
    path = prefixWithSlash(path);
    result.put("branch", branch);
    result.put("skinbase", skinBase);
    result.put("branchurl", getBranchUrl(request));
    result.put("contextpath", request.getContextPath());
    result.put("path", path);
    result.put("title", getTitle(request));
    result.put("refresh", getRefreshTimestamp(getContentManager()));
    return result;
  }
}
