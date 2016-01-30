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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
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

public abstract class DocServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LoggerFactory.getLogger(DocServlet.class);

  private static final String CLASSPATH_PREFIX = "classpath:";
  private static final String VELOCITY_HELPER = "thothutil";
  private static final String SKINS_PROPERTIES = "skins.properties";
  private static final String TIMEMSTAMP_FORMAT = "dd-MM-yyyy HH:mm:ss";
  private static final String VELOCITY_PROPERTIES = "net/riezebos/thoth/velocity.properties";
  protected static final String NATIVERESOURCES = "/nativeresources/";

  protected abstract void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ContentManagerException;

  public DocServlet() {
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

  protected String getAbsolutePath(HttpServletRequest request) throws ServletException, BranchNotFoundException, IOException {
    String path = getPath(request);
    String branch = getBranch(request);
    return getContentManager().getAbsolutePath(branch, path);
  }

  protected String getTitle(HttpServletRequest request) {
    String path = getPath(request);
    if (!StringUtils.isBlank(path)) {
      int idx = path.lastIndexOf("/");
      if (idx != -1)
        path = path.substring(idx + 1);
      idx = path.lastIndexOf('.');
      if (idx != -1)
        path = path.substring(0, idx);
    }
    return path;
  }

  protected String getPath(HttpServletRequest request) {
    String path = request.getServletPath();
    if (path != null && path.startsWith("/")) {
      path = path.substring(1);

      int idx = path.indexOf('/');
      if (idx != -1)
        path = path.substring(idx + 1);
      if (path.indexOf("/") == -1)
        path = "";
    }
    return path;
  }

  protected String getBranchUrl(HttpServletRequest request) {
    return request.getContextPath() + "/" + getBranch(request);
  }

  protected String getBranch(HttpServletRequest request) {
    String path = request.getServletPath();
    if (path != null && path.startsWith("/")) {
      path = path.substring(1);

      int idx = path.indexOf('/');
      if (idx != -1)
        path = path.substring(0, idx);
    }
    return path;
  }

  protected Map<String, Object> getVariables(HttpServletRequest request) throws ServletException {
    Map<String, Object> result = new HashMap<>();
    String branch = getBranch(request);
    Skin skin = getSkin(request);
    String skinBase;
    String baseUrl = skin.getBaseUrl();
    if (skin.isFromClassPath()) {
      skinBase = NATIVERESOURCES + baseUrl;
    } else {
      skinBase = request.getContextPath() + baseUrl;
      if (!skinBase.startsWith("/"))
        skinBase = "/" + skinBase;
    }

    result.put("branch", branch);
    result.put("skinbase", skinBase);
    result.put("branchurl",

        getBranchUrl(request));
    result.put("contextpath", request.getContextPath());
    result.put("path", getPath(request));
    result.put("title", getTitle(request));
    result.put("refresh", getRefreshTimestamp(getContentManager()));
    return result;
  }

  protected String getRefreshTimestamp(ContentManager contentManager) throws ServletException {
    String refresh =
        getContentManager().getLatestRefresh() == null ? "Never" : new SimpleDateFormat(TIMEMSTAMP_FORMAT).format(contentManager.getLatestRefresh());
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
        skinMappings = new ArrayList<>();

        String branchFolder = ContentManagerFactory.getContentManager().getBranchFolder(branch);
        String skinMappingFileName = branchFolder + SKINS_PROPERTIES;
        File skinMappingFile = new File(skinMappingFileName);
        if (!skinMappingFile.isFile()) {
          LOG.warn("No " + SKINS_PROPERTIES + " properties file found at " + skinMappingFileName + " so falling back to built in which is "
              + configuration.getDefaultSkin());
          skinMappings.add(new SkinMapping(Pattern.compile(".*"), new Skin(branch, configuration.getDefaultSkin())));
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
        throw new IllegalArgumentException("No skin mapping defined for request " + path);
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

  protected void executeVelocityTemplate(String template, String branch, Map<String, Object> variables, HttpServletResponse response)
      throws ContentManagerException, IOException, UnsupportedEncodingException {
    try (PrintWriter writer = response.getWriter()) {
      VelocityContext context = new VelocityContext(variables);
      context.put(VELOCITY_HELPER, new ThothUtil());
      VelocityEngine engine = new VelocityEngine();
      Properties properties = new Properties();
      properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(VELOCITY_PROPERTIES));
      engine.init(properties);
      engine.getTemplate(template).merge(context, writer);
    } catch (Exception e) {
      throw new ContentManagerException(e);
    }
  }

  protected void executeSimpleTemplate(PrintWriter writer, String location, HttpServletRequest request, Map<String, Object> arguments)
      throws BranchNotFoundException, ContentManagerException, IOException, ServletException {
    if (!StringUtils.isBlank(location)) {

      InputStream is = null;

      if (location.startsWith(CLASSPATH_PREFIX)) {
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(location.substring(CLASSPATH_PREFIX.length()));
      }
      File templateFile = new File(location);
      if (templateFile.exists())
        is = new FileInputStream(templateFile);

      if (is == null) {
        writer.print("Template file " + location + " not found\n");
      } else {
        Map<String, Object> variables = getVariables(request);
        variables.putAll(arguments);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
        IOUtils.copy(is, bos);
        String template = ThothUtil.replaceKeywords(bos.toString("UTF-8"), variables);
        writer.print(template);
      }
    }
  }

}
