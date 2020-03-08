/* Copyright (c) 2020 W.T.J. Riezebos
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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.commands.CommandOperation;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class HtmlRendererTest extends RendererTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    String contextName = "TestContext";
    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    ContentManager contentManager = createTestContentManager(thothEnvironment, contextName);
    Skin testSkin = getSkin(contentManager);

    HtmlRenderer renderer = new HtmlRenderer(thothEnvironment, this);

    String path = "/main/Fourth.md";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Map<String, Object> arguments = getParameters(contentManager, path);
    renderer.execute(getCurrentUser(thothEnvironment), contextName, path, CommandOperation.GET, arguments, testSkin, outputStream);
    String result = outputStream.toString("UTF-8").trim();
    String expected = getExpected("Fourth.expected.html");
    assertTrue(stringsEqual(expected, result));
    assertEquals("text/html;charset=UTF-8", renderer.getContentType(new HashMap<String, Object>()));
    assertEquals(HtmlRenderer.TYPE, renderer.getTypeCode());

    RenderResult renderResult =
        renderer.execute(getCurrentUser(thothEnvironment), contextName, "/wrong/path.md", CommandOperation.GET, arguments, testSkin, outputStream);
    assertEquals(RenderResult.NOT_FOUND, renderResult);
  }

  @Test
  public void testTable() throws ContextNotFoundException, ContentManagerException, IOException {

    String contextName = "TestContext";
    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    ContentManager contentManager = createTestContentManager(thothEnvironment, contextName);

    HtmlRenderer renderer = new HtmlRenderer(thothEnvironment, this);

    String path = "/main/Table.md";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Map<String, Object> arguments = getParameters(contentManager, path);
    renderer.execute(getCurrentUser(thothEnvironment), contextName, path, CommandOperation.GET, arguments, getSkin(contentManager), outputStream);
    String result = outputStream.toString("UTF-8").trim();
    String expected = getExpected("Table.expected.html");
    assertTrue(stringsEqual(expected, result));
  }

}
