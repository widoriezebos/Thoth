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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.configuration.CacheManager;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerBase;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.content.impl.ClasspathContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.util.ThothUtil;

public class ThothTestBase {

  private Map<String, Object> parameters = new HashMap<>();
  private String latestContentType;
  private Integer latestError;

  protected ContentManager getContentManager(Configuration mockedConfiguration, ContextDefinition mockedContext, ClasspathFileSystem fileSystem)
      throws ContentManagerException {
    ContentManagerBase contentManager = new ClasspathContentManager(mockedContext, mockedConfiguration, fileSystem);
    return contentManager;
  }

  protected ClasspathFileSystem getClasspathFileSystem() throws IOException {
    String location = "net/riezebos/thoth/content/testrepos";
    ClasspathFileSystem fileSystem = new ClasspathFileSystem(location);
    fileSystem.registerFiles("net/riezebos/thoth/content/testrepos.lst");
    return fileSystem;
  }

  protected ContentManager registerTestContentManager(String contextName) throws ContextNotFoundException, ContentManagerException, IOException {
    CacheManager mockedCacheManager = mockCacheManager();
    Configuration mockedConfiguration = mockConfiguration(mockedCacheManager, contextName);
    ContextDefinition mockedContext = mockContextDefinition(contextName);
    ClasspathFileSystem fileSystem = getClasspathFileSystem();
    ContentManager contentManager = getContentManager(mockedConfiguration, mockedContext, fileSystem);
    ContentManagerFactory.registerContentManager(contentManager);
    return contentManager;
  }

  protected ContextDefinition mockContextDefinition(String contextName) {
    ContextDefinition mockedContext = mock(ContextDefinition.class);
    when(mockedContext.getName()).thenReturn(contextName);
    when(mockedContext.getRefreshIntervalMS()).thenReturn(0L);
    return mockedContext;
  }

  protected Configuration mockConfiguration(CacheManager mockedCacheManager, String contextName) throws ContextNotFoundException {
    Configuration mockedConfiguration = mock(Configuration.class);
    when(mockedConfiguration.getWorkspaceLocation()).thenReturn("/some/workspace/");
    when(mockedConfiguration.getDefaultSkin()).thenReturn("SimpleSkin");
    when(mockedConfiguration.appendErrors()).thenReturn(true);
    when(mockedConfiguration.getBookExtensions()).thenReturn(Arrays.asList(new String[] {"book"}));
    when(mockedConfiguration.getDocumentExtensions()).thenReturn(Arrays.asList(new String[] {"md"}));
    when(mockedConfiguration.getImageExtensions()).thenReturn("png");
    when(mockedConfiguration.isFragment(anyString())).thenReturn(true);
    when(mockedConfiguration.getDateFormat()).thenReturn(new SimpleDateFormat("dd-MM-yyyy"));
    when(mockedConfiguration.getTimestampFormat()).thenReturn(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
    when(mockedConfiguration.getCacheManager(anyString())).thenReturn(mockedCacheManager);
    when(mockedConfiguration.getParseTimeOut()).thenReturn(4000L);
    when(mockedConfiguration.getMarkdownOptions()).thenReturn(2098159);
    when(mockedConfiguration.isValidContext(contextName)).thenReturn(true);
    when(mockedConfiguration.getMainIndexSkinContext()).thenReturn(contextName);
    when(mockedConfiguration.getContexts()).thenReturn(Arrays.asList(contextName));
    return mockedConfiguration;
  }

  protected CacheManager mockCacheManager() throws ContextNotFoundException, ContentManagerException {
    CacheManager mockedCacheManager = mock(CacheManager.class);
    when(mockedCacheManager.getReverseIndex(true)).thenReturn(getReverseIndexIndirect());
    when(mockedCacheManager.getReverseIndex(false)).thenReturn(getReverseIndex());
    return mockedCacheManager;
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
    InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    if (resourceAsStream == null)
      throw new FileNotFoundException(path + " not found");
    return ThothUtil.readInputStream(resourceAsStream).trim();
  }

  protected byte[] getExpectedBytes(String path) throws IOException {
    path = "net/riezebos/thoth/content/expected/" + ThothUtil.stripPrefix(path, "/");
    return getBytes(path);
  }

  protected byte[] getBytes(String path) throws FileNotFoundException, IOException {
    path = ThothUtil.stripPrefix(path, "/");
    InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    if (resourceAsStream == null)
      throw new FileNotFoundException(path + " not found");
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

  protected Map<String, List<String>> getReverseIndex() {
    Map<String, List<String>> result = new HashMap<>();
    result.put("/main/subs/SubOne.md", Arrays.asList(new String[] {"/main/Main.md"}));
    result.put("/main/Main.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Second.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Second.book"}));
    result.put("/main/Third.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Fourth.md", Arrays.asList(new String[] {"/books/Second.book"}));
    return result;
  }

  protected Map<String, List<String>> getReverseIndexIndirect() {
    Map<String, List<String>> result = new HashMap<>();
    result.put("/main/subs/SubOne.md", Arrays.asList(new String[] {"/main/Main.md", "/books/Main.book"}));
    result.put("/main/Main.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Second.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Second.book"}));
    result.put("/main/Third.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Main.book"}));
    result.put("/main/Fourth.md", Arrays.asList(new String[] {"/books/Second.book"}));
    return result;
  }

  protected List<ContentNode> getNodes(ClasspathFileSystem factory, String... paths) {
    List<ContentNode> result = new ArrayList<>();
    for (String path : paths) {
      FileHandle fileHandle = factory.getFileHandle(path);
      result.add(new ContentNode(fileHandle.getAbsolutePath(), fileHandle));
    }
    return result;
  }

  protected Map<String, Object> getParameters(Configuration configuration, String contextName, Skin skin, String path) {
    Map<String, Object> result = new HashMap<>();

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
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        return ThothTestBase.this.getParameter(args);
      }
    }).when(response).getParameter(anyString());
  }

  protected void recordSendError(HttpServletResponse response) throws IOException {
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        ThothTestBase.this.sendError(args);
        return null;
      }
    }).when(response).sendError(anyInt());
  }

  protected void recordSetContentType(HttpServletResponse response) throws IOException {
    doAnswer(new Answer<Object>() {
      public Object answer(InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        ThothTestBase.this.setContentType(args);
        return null;
      }
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
}
