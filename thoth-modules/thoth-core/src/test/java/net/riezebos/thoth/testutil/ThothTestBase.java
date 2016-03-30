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
package net.riezebos.thoth.testutil;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.TestCacheManager;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.AccessManager;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerBase;
import net.riezebos.thoth.content.impl.ClasspathContentManager;
import net.riezebos.thoth.content.impl.FSContentManager;
import net.riezebos.thoth.content.impl.util.TestFSContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.ContextManager;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.context.RepositoryType;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.User;
import net.riezebos.thoth.user.UserManager;
import net.riezebos.thoth.util.ThothUtil;

public class ThothTestBase {

  private Map<String, Object> parameters = new HashMap<>();
  private String latestContentType;
  private Integer latestError;

  protected ThothEnvironment createThothContext(String contextName) throws ContextNotFoundException, ContentManagerException {
    ThothEnvironment thothEnvironment = new ThothEnvironment();
    Configuration mockedConfiguration = mockConfiguration(contextName);
    thothEnvironment.setConfiguration(mockedConfiguration);
    thothEnvironment.setUserManager(getTestUserManager());
    thothEnvironment.setContextManager(getTestContextManager(thothEnvironment));
    return thothEnvironment;
  }

  protected ContextManager getTestContextManager(ThothEnvironment thothEnvironment) {
    TestContextManager testContextManager = new TestContextManager(thothEnvironment);
    testContextManager.allIsValidFromNowOn();
    return testContextManager;
  }

  protected UserManager getTestUserManager() {
    return new TestUserManager();
  }

  protected ContentManager createTestContentManager(ThothEnvironment thothEnvironment, String contextName) throws IOException, ContentManagerException {
    ContextDefinition mockedContext = mockContextDefinition(contextName);
    ClasspathFileSystem fileSystem = getClasspathFileSystem();
    ContentManagerBase contentManager = new ClasspathContentManager(mockedContext, thothEnvironment, fileSystem);
    thothEnvironment.registerContentManager(contentManager);
    contentManager.setCacheManager(new TestCacheManager(contentManager));
    AccessManager lenientAccessManager = new AccessManager(contentManager) {
      @Override
      public boolean hasPermission(Identity identity, String path, net.riezebos.thoth.user.Permission requestedPermission) {
        return true;
      };
    };
    contentManager.setAccessManager(lenientAccessManager);
    return contentManager;
  }

  protected FSContentManager createTempFSContentManager(String contextName) throws IOException, ContentManagerException {
    ThothEnvironment thothEnvironment = createThothContext(contextName);
    File tmpFile = File.createTempFile("thoth", "test");
    tmpFile.deleteOnExit();
    String fsroot = ThothUtil.suffix(ThothUtil.normalSlashes(tmpFile.getParent()), "/") + "fstestroot/";
    File fsRootFile = new File(fsroot);
    fsRootFile.mkdirs();
    fsRootFile.deleteOnExit();

    RepositoryDefinition repodef = new RepositoryDefinition();
    repodef.setLocation(fsroot);
    repodef.setName("testrepos");
    repodef.setType(RepositoryType.FILESYSTEM);

    ContextDefinition contextDef = new ContextDefinition(repodef, "testfs", "branch", "", 0);

    FSContentManager contentManager = new TestFSContentManager(contextDef, thothEnvironment);
    return contentManager;
  }

  protected ClasspathFileSystem getClasspathFileSystem() throws IOException {
    String location = "net/riezebos/thoth/content/testrepos";
    ClasspathFileSystem fileSystem = new ClasspathFileSystem(location);
    fileSystem.registerFiles("net/riezebos/thoth/content/testrepos.lst");
    return fileSystem;
  }

  protected ContextDefinition mockContextDefinition(String contextName) {
    RepositoryDefinition mockedRepos = mock(RepositoryDefinition.class);
    when(mockedRepos.getLocation()).thenReturn("/");
    when(mockedRepos.getName()).thenReturn("MockedRepos");
    when(mockedRepos.getType()).thenReturn(RepositoryType.NOP);

    ContextDefinition mockedContext = mock(ContextDefinition.class);
    when(mockedContext.getRepositoryDefinition()).thenReturn(mockedRepos);
    when(mockedContext.getName()).thenReturn(contextName);
    when(mockedContext.getRefreshInterval()).thenReturn(0L);
    return mockedContext;
  }

  protected Configuration mockConfiguration(String contextName) throws ContextManagerException {

    ContextDefinition def = mockContextDefinition(contextName);
    Map<String, ContextDefinition> map = new HashMap<>();
    map.put(contextName, def);

    Configuration mockedConfiguration = mock(Configuration.class);
    when(mockedConfiguration.getWorkspaceLocation()).thenReturn("/some/workspace/");
    when(mockedConfiguration.getDefaultSkin()).thenReturn("SimpleSkin");
    when(mockedConfiguration.appendErrors()).thenReturn(true);
    when(mockedConfiguration.getBookExtensions()).thenReturn(Arrays.asList(new String[] {"book"}));
    when(mockedConfiguration.getDocumentExtensions()).thenReturn(Arrays.asList(new String[] {"md"}));
    when(mockedConfiguration.getImageExtensions()).thenReturn("png");
    when(mockedConfiguration.getIndexExtensions()).thenReturn("md,book");
    when(mockedConfiguration.isFragment(anyString())).thenReturn(true);
    when(mockedConfiguration.getDateFormat()).thenReturn(new SimpleDateFormat("dd-MM-yyyy"));
    when(mockedConfiguration.getTimestampFormat()).thenReturn(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
    when(mockedConfiguration.getParseTimeOut()).thenReturn(4000L);
    when(mockedConfiguration.getMarkdownOptions()).thenReturn(2098159);
    when(mockedConfiguration.getMainIndexSkinContext()).thenReturn(contextName);
    when(mockedConfiguration.getConfiguredContextDefinitions()).thenReturn(map);
    when(mockedConfiguration.addNewlineBeforeheader()).thenReturn(true);
    when(mockedConfiguration.getDefaultGroup()).thenReturn("administrators");
    when(mockedConfiguration.getDatabaseUser()).thenReturn("thoth");
    when(mockedConfiguration.getDatabasePassword()).thenReturn("thoth");
    when(mockedConfiguration.getDatabaseUrl()).thenReturn("/some/workspace/db");
    when(mockedConfiguration.getDatabaseType()).thenReturn("embedded");
    return mockedConfiguration;
  }

  protected HttpServletRequest createHttpRequest(String contextName, String path) throws IOException {
    String fullPath = ThothUtil.prefix(contextName, "/") + path;
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn(fullPath);
    when(request.getContextPath()).thenReturn(ThothUtil.prefix(contextName, "/"));
    when(request.getServletPath()).thenReturn(fullPath);
    when(request.getPathInfo()).thenReturn(fullPath);
    when(request.getParameterNames()).thenReturn(getParameterNames());
    recordGetParameter(request);
    return request;
  }

  protected HttpServletResponse createHttpResponse() throws IOException {
    HttpServletResponse response = mock(HttpServletResponse.class);
    MockServletOutputStream sos = new MockServletOutputStream();
    when(response.getOutputStream()).thenReturn(sos);
    recordSendError(response);
    recordSetContentType(response);
    resetContentAndError();
    return response;
  }

  protected String getExpected(String path) throws IOException {
    path = "net/riezebos/thoth/content/expected/" + ThothUtil.stripPrefix(path, "/");
    InputStream resourceAsStream = getClassPathResource(path);
    return ThothUtil.readInputStream(resourceAsStream).trim();
  }

  protected InputStream getClassPathResource(String path) throws FileNotFoundException {
    InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    if (resourceAsStream == null)
      throw new FileNotFoundException(path + " not found");
    return resourceAsStream;
  }

  protected byte[] getExpectedBytes(String path) throws IOException {
    path = "net/riezebos/thoth/content/expected/" + ThothUtil.stripPrefix(path, "/");
    return getBytes(path);
  }

  protected byte[] getBytes(String path) throws FileNotFoundException, IOException {
    path = ThothUtil.stripPrefix(path, "/");
    InputStream resourceAsStream = getClassPathResource(path);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtils.copy(resourceAsStream, bos);
    return bos.toByteArray();
  }

  protected String getAsString(ContentManager contentManager, String path) throws IOException {
    InputStream inputStream = contentManager.getInputStream(path);
    if (inputStream == null)
      return null;
    return ThothUtil.readInputStream(inputStream);
  }

  protected List<ContentNode> getNodes(ClasspathFileSystem factory, String... paths) {
    List<ContentNode> result = new ArrayList<>();
    for (String path : paths) {
      FileHandle fileHandle = factory.getFileHandle(path);
      result.add(new ContentNode(fileHandle.getAbsolutePath(), fileHandle));
    }
    return result;
  }

  protected Skin getSkin(ContentManager contentManager) throws SkinManagerException {
    SkinManager skinManager = contentManager.getSkinManager();
    return skinManager.getSkinByName("TestReposSkin1");
  }

  protected Map<String, Object> getParameters(ContentManager contentManager, String path) throws SkinManagerException {
    Map<String, Object> result = new HashMap<>();

    String contextName = contentManager.getContextName();
    Configuration configuration = contentManager.getConfiguration();

    Skin skin = getSkin(contentManager);
    String skinBase = null;
    if (skin != null) {
      String baseUrl = skin.getBaseUrl();
      if (skin.isFromClassPath()) {
        skinBase = ContentManager.NATIVERESOURCES + ThothUtil.prefix(baseUrl, "/");
      } else {
        skinBase = ThothUtil.prefix(baseUrl, "/");
      }
    }

    Date now = getDate("15-02-2016 21:39:00");
    path = ThothUtil.prefix(path, "/");
    result.put(Renderer.CONTEXT_PARAMETER, contextName);
    result.put(Renderer.SKINBASE_PARAMETER, skinBase);
    result.put(Renderer.CONTEXTURL_PARAMETER, ThothUtil.prefix(contextName, "/"));
    result.put(Renderer.CONTEXTPATH_PARAMETER, ThothUtil.prefix(contextName, "/") + ThothUtil.prefix(path, "/"));
    result.put(Renderer.PATH_PARAMETER, ThothUtil.prefix(path, "/"));
    result.put(Renderer.TITLE_PARAMETER, ThothUtil.getNameOnly(path));
    result.put(Renderer.SKIN, skin == null ? null : skin.getName());
    result.put(Renderer.TODAY, configuration.getDateFormat().format(now));
    result.put(Renderer.NOW, configuration.getTimestampFormat().format(now));
    result.put(Renderer.REFRESH_PARAMETER, configuration.getTimestampFormat().format(now));
    return result;
  }

  protected Date getDate(String someDate) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    Date now;
    try {
      now = sdf.parse(someDate);
    } catch (ParseException e) {
      throw new IllegalArgumentException(someDate);
    }
    return now;
  }

  protected void recordGetParameter(HttpServletRequest response) throws IOException {
    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      return ThothTestBase.this.getParameter(args);
    }).when(response).getParameter(anyString());
  }

  protected void recordSendError(HttpServletResponse response) throws IOException {
    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      ThothTestBase.this.sendError(args);
      return null;
    }).when(response).sendError(anyInt());
  }

  protected void recordSetContentType(HttpServletResponse response) throws IOException {
    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      ThothTestBase.this.setContentType(args);
      return null;
    }).when(response).setContentType(anyString());
  }

  protected void resetContentAndError() {
    latestContentType = null;
    latestError = null;
  }

  protected String getLatestContentType() {
    return latestContentType;
  }

  protected void setContentType(Object[] args) {
    latestContentType = (String) args[0];
  }

  public Integer getLatestError() {
    return latestError;
  }

  protected void sendError(Object[] args) {
    latestError = (Integer) args[0];
  }

  protected Enumeration<String> getParameterNames() {
    return Collections.enumeration(new HashSet<>(parameters.keySet()));
  }

  protected Object getParameter(Object[] args) {
    Object key = args[0];
    Object result = parameters.get(key);
    return result;
  }

  protected void setRequestParameter(String name, Object value) {
    parameters.put(name, value);
  }

  protected User getCurrentUser(ThothEnvironment thothEnvironment) throws UserManagerException {
    UserManager userManager = thothEnvironment.getUserManager();
    return userManager.getUser("administrator");
  }

  protected boolean stringsEqual(String str1, String str2) {
    if (str1 == null && str2 == null)
      return true;
    if (str1 == null || str2 == null)
      return false;
    str1 = str1.replaceAll("\r", "");
    str2 = str2.replaceAll("\r", "");
    return str1.equals(str2);
  }

  protected void cleanupTempFolder(File dbDir) throws IOException {
    File tempFile = File.createTempFile("safe", "check");
    if (!tempFile.getParentFile().getAbsolutePath().equals(dbDir.getParentFile().getAbsolutePath())) {
      tempFile.delete();
      throw new IllegalArgumentException("Refuse to clean anything outside the tempfolder");
    }
    tempFile.delete();
    unsafeCleanupTempFolder(dbDir);
  }

  private void unsafeCleanupTempFolder(File dbDir) {
    if (dbDir.isDirectory()) {
      File[] listFiles = dbDir.listFiles();
      if (listFiles != null)
        for (File child : listFiles)
          unsafeCleanupTempFolder(child);
      dbDir.delete();
    } else if (dbDir.isFile())
      dbDir.delete();
  }

}
