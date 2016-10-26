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
package net.riezebos.thoth.content.skinning;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.testutil.ThothTestBase;

public class SkinManagerTest extends ThothTestBase {

  @Test
  public void test() throws IOException, ContentManagerException {

    Skin dummy = new Skin();
    assertEquals("/", dummy.getBaseUrl());
    assertEquals("/", dummy.getContext());

    ClasspathFileSystem factory = new ClasspathFileSystem();
    factory.registerFiles("net/riezebos/thoth/content/skinning/files/resources.lst");

    String skinsProperties = "/net/riezebos/thoth/content/skinning/files/skins.properties";
    String skin1 = "/net/riezebos/thoth/content/skinning/files/testskin/skin.properties";
    String skin2 = "/net/riezebos/thoth/content/skinning/files/testskin2/skin.properties";

    FileHandle skinsHandle = factory.getFileHandle(skinsProperties);
    FileHandle skinHandle1 = factory.getFileHandle(skin1);
    FileHandle skinHandle2 = factory.getFileHandle(skin2);

    Configuration mockConfiguration = mockConfiguration("test");
    ContentManager mockedContentManager = mock(ContentManager.class);
    when(mockedContentManager.getContextName()).thenReturn("testcontext");
    when(mockedContentManager.getFileHandle("/skins.properties")).thenReturn(skinsHandle);
    when(mockedContentManager.getFileHandle(skin1)).thenReturn(skinHandle1);
    when(mockedContentManager.getFileHandle(skin2)).thenReturn(skinHandle2);
    when(mockedContentManager.find("skin.properties", true)).thenReturn(getNodes(factory, skin1, skin2));
    when(mockedContentManager.getConfiguration()).thenReturn(mockConfiguration);

    SkinManager skinManager = new SkinManager(mockedContentManager, null);
    Skin simpleSkin = skinManager.getSkinByName("SimpleSkin");
    Skin testSkin = skinManager.getSkinByName("TestSkin");
    Skin testSkin2 = skinManager.getSkinByName("TestSkin2");
    List<SkinMapping> skinMappings = skinManager.getSkinMappings();
    assertEquals("SimpleSkin", simpleSkin.getName());
    assertEquals(simpleSkin, skinManager.getDefaultSkin());
    assertEquals("TestSkin", testSkin.getName());
    assertEquals("/net/riezebos/thoth/content/skinning/files/testskin/", testSkin.getSkinBaseFolder());
    assertEquals("testcontext/net/riezebos/thoth/content/skinning/files/testskin", testSkin.getBaseUrl());
    assertEquals(simpleSkin, testSkin.getSuper());
    assertEquals(3, skinMappings.size());
    assertEquals("\\/a\\/b(.*?)", skinMappings.get(0).getPattern().toString());
    assertEquals("\\/a(.*?)", skinMappings.get(1).getPattern().toString());
    assertEquals("(.*?)", skinMappings.get(2).getPattern().toString());

    String pfx1 = "/net/riezebos/thoth/content/skinning/files/testskin/";
    String pfx2 = "/net/riezebos/thoth/content/skinning/files/testskin2/";

    assertEquals("classpath:/net/riezebos/thoth/skins/simpleskin/browse.tpl", testSkin.getBrowseTemplate());
    assertEquals("classpath:/net/riezebos/thoth/skins/simpleskin/error.tpl", testSkin.getErrorTemplate());
    assertEquals("classpath:/net/riezebos/thoth/skins/simpleskin/search.tpl", testSkin.getSearchTemplate());
    assertEquals(pfx1 + "testindex.tpl", testSkin.getIndexTemplate());
    assertEquals(pfx1 + "testcontextindex.tpl", testSkin.getContextIndexTemplate());
    assertEquals(pfx1 + "testdiff.tpl", testSkin.getDiffTemplate());
    assertEquals(pfx1 + "testhtml.tpl", testSkin.getHtmlTemplate());
    assertEquals(pfx1 + "testmeta.tpl", testSkin.getMetaInformationTemplate());
    assertEquals(pfx1 + "testrevisions.tpl", testSkin.getRevisionTemplate());
    assertEquals(pfx1 + "testvalidationreport.tpl", testSkin.getValidationTemplate());

    assertEquals("classpath:/net/riezebos/thoth/skins/simpleskin/browse.tpl", testSkin2.getBrowseTemplate());
    assertEquals("classpath:/net/riezebos/thoth/skins/simpleskin/error.tpl", testSkin2.getErrorTemplate());
    assertEquals(pfx1 + "testdiff.tpl", testSkin2.getDiffTemplate());
    assertEquals(pfx1 + "testhtml.tpl", testSkin2.getHtmlTemplate());
    assertEquals(pfx1 + "testmeta.tpl", testSkin2.getMetaInformationTemplate());
    assertEquals(pfx1 + "testrevisions.tpl", testSkin2.getRevisionTemplate());
    assertEquals(pfx1 + "testvalidationreport.tpl", testSkin2.getValidationTemplate());
    assertEquals(pfx2 + "test2index.tpl", testSkin2.getIndexTemplate());
    assertEquals(pfx2 + "test2contextindex.tpl", testSkin2.getContextIndexTemplate());

    String inheritedPath = skinManager.getInheritedPath("net/riezebos/thoth/content/skinning/files/testskin2/Webresources/a.txt");
    assertEquals("/net/riezebos/thoth/content/skinning/files/testskin/Webresources/a.txt", inheritedPath);
    String inheritedPath2 = skinManager.getInheritedPath("net/riezebos/thoth/content/skinning/files/testskin/Webresources/a.txt");
    assertEquals("classpath:/net/riezebos/thoth/skins/simpleskin/Webresources/a.txt", inheritedPath2);

    assertEquals(simpleSkin, skinManager.determineSkin("/something"));
    assertEquals(testSkin, skinManager.determineSkin("/a/c/something"));
    assertEquals(testSkin2, skinManager.determineSkin("/a/b/something"));

    when(mockedContentManager.getFileHandle("/skins.properties")).thenReturn(factory.getFileHandle("wrong"));
    skinManager = new SkinManager(mockedContentManager, SkinManager.SKIN_PARENT_OF_ALL);
    skinMappings = skinManager.getSkinMappings();
    assertEquals(1, skinMappings.size());
    assertEquals("(.*?)", skinMappings.get(0).getPattern().toString());
  }
}
