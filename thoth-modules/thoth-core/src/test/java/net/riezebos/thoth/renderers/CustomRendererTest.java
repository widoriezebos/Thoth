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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.commands.CommandOperation;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;

public class CustomRendererTest extends RendererTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {
    String contextName = "TestContext";
    String contentType = "application/pdf";
    String extension = "pdf";
    String source = "html";
    String commandLine = "output={${output}} url={${url}}";

    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    ContentManager contentManager = createTestContentManager(thothEnvironment, contextName);

    TestRendererProvider testRendererProvider = new TestRendererProvider(thothEnvironment);

    CustomRendererDefinition def = new CustomRendererDefinition(extension, contentType, source, commandLine);
    TestCustomRenderer renderer = new TestCustomRenderer(thothEnvironment, def, testRendererProvider);

    String path = "/main/Fourth.md";
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Map<String, Object> arguments = getParameters(contentManager, path);
    renderer.execute(getCurrentUser(thothEnvironment), contextName, path, CommandOperation.GET, arguments, getSkin(contentManager), outputStream);
    String result = outputStream.toString("UTF-8").trim();
    assertEquals("rendered", result);
    assertEquals(contentType, renderer.getContentType(new HashMap<String, Object>()));
    assertEquals(extension, renderer.getTypeCode());

  }

}
