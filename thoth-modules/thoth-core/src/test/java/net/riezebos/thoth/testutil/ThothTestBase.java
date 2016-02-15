package net.riezebos.thoth.testutil;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.configuration.CacheManager;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerBase;
import net.riezebos.thoth.content.impl.ClasspathContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.renderers.Renderer;
import net.riezebos.thoth.util.ThothUtil;

public class ThothTestBase {

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

  protected ContextDefinition mockContextDefinition(String contextName) {
    ContextDefinition mockedContext = mock(ContextDefinition.class);
    when(mockedContext.getName()).thenReturn(contextName);
    when(mockedContext.getRefreshIntervalMS()).thenReturn(0L);
    return mockedContext;
  }

  protected Configuration mockConfiguration(CacheManager mockedCacheManager) throws ContextNotFoundException {
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
    return mockedConfiguration;
  }

  protected CacheManager mockCacheManager() throws ContextNotFoundException, ContentManagerException {
    CacheManager mockedCacheManager = mock(CacheManager.class);
    when(mockedCacheManager.getReverseIndex(true)).thenReturn(getReverseIndexIndirect());
    when(mockedCacheManager.getReverseIndex(false)).thenReturn(getReverseIndex());
    return mockedCacheManager;
  }

  protected String getExpected(String path) throws IOException {
    path = "net/riezebos/thoth/content/expected/" + path;
    InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    if (resourceAsStream == null)
      throw new FileNotFoundException(path + " not found");
    return ThothUtil.readInputStream(resourceAsStream).trim();
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
}
