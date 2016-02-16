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
package net.riezebos.thoth.renderers;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.renderers.Renderer.RenderResult;
import net.riezebos.thoth.testutil.ThothTestBase;

public class RawRendererTest extends ThothTestBase {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    String contextName = "TestContext";

    ContentManager contentManager = registerTestContentManager(contextName);
    Configuration configuration = contentManager.getConfiguration();
    SkinManager skinManager = contentManager.getSkinManager();
    Skin testSkin = skinManager.getSkinByName("TestReposSkin1");

    RawRenderer renderer = new RawRenderer();
    renderer.setConfiguration(configuration);

    String path = "/main/Fourth.md";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Map<String, Object> arguments = getParameters(configuration, contextName, testSkin, path);
    renderer.execute(contextName, path, arguments, testSkin, outputStream);
    String result = outputStream.toString("UTF-8").trim();
    String expected = getExpected("Fourth.expected.md");
    assertEquals(expected, result);
    assertEquals("text/plain;charset=UTF-8", renderer.getContentType(new HashMap<String, Object>()));
    assertEquals(RawRenderer.TYPE, renderer.getTypeCode());

    RenderResult renderResult = renderer.execute(contextName, "/wrong/path.md", arguments, testSkin, outputStream);
    assertEquals(RenderResult.NOT_FOUND, renderResult);
  }

  @Test
  public void testBase() throws ContextNotFoundException, ContentManagerException, IOException {

    RawRenderer renderer = new RawRenderer();
    Map<String, Object> map = new HashMap<>();
    map.put("num", "123");
    assertEquals(new Integer(123), renderer.getInteger(map, "num"));
  }

}
