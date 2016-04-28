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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import net.riezebos.thoth.commands.Command;
import net.riezebos.thoth.commands.CommandOperation;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.RenderResult;
import net.riezebos.thoth.testutil.MockServletOutputStream;
import net.riezebos.thoth.testutil.ThothTestBase;
import net.riezebos.thoth.user.Identity;

public class ThothServletTest extends ThothTestBase {

  @Test
  public void test() throws ServletException, IOException, ContextNotFoundException, ContentManagerException {

    String contextName = "TestContext";
    String path = "/main/Fourth.md";

    setRequestParameter("output", "raw");
    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    createTestContentManager(thothEnvironment, contextName);

    HttpServletRequest request = createHttpRequest(contextName, path);
    HttpServletResponse response = createHttpResponse();

    ThothServlet thothServlet = new ThothServlet();
    thothServlet.setThothContext(thothEnvironment);
    thothServlet.init();

    thothServlet.doGet(request, response);
    MockServletOutputStream sos = (MockServletOutputStream) response.getOutputStream();
    String actual = sos.getContentsAsString().trim();
    String expected = getExpected("Fourth.expected.md");
    assertTrue(stringsEqual(expected, actual));
    assertEquals("text/plain;charset=UTF-8", getLatestContentType());
    assertNull(getLatestError());

    request = createHttpRequest(contextName, "/images/tip.png");
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    sos = (MockServletOutputStream) response.getOutputStream();
    assertArrayEquals(getExpectedBytes("/tip.png"), sos.getContents());
    assertNull(getLatestError());

    request = createHttpRequest(contextName, "/isnotthere.md");
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    sos = (MockServletOutputStream) response.getOutputStream();
    assertEquals(new Integer(404), getLatestError());

    request = createHttpRequest(contextName, "/isnotthere.abc");
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    sos = (MockServletOutputStream) response.getOutputStream();
    assertEquals(new Integer(404), getLatestError());

    request = createHttpRequest("invalid", "/images/tip.png");
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    assertEquals(new Integer(HttpServletResponse.SC_NOT_FOUND), getLatestError());

    String skinIcon = "/net/riezebos/thoth/skins/simpleskin/Webresources/favicon.png";
    request = createHttpRequest("nativeresources", skinIcon);
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    sos = (MockServletOutputStream) response.getOutputStream();
    assertArrayEquals(getBytes(skinIcon), sos.getContents());
    assertNull(getLatestError());

    String disallowed = "/net/riezebos/thoth/content/search/Fragment.class";
    request = createHttpRequest("nativeresources", disallowed);
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    assertEquals(new Integer(HttpServletResponse.SC_FORBIDDEN), getLatestError());

    String notfound = "/net/riezebos/thoth/skins/simpleskin/Webresources/notthere.abc";
    request = createHttpRequest("nativeresources", notfound);
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    assertEquals(new Integer(HttpServletResponse.SC_NOT_FOUND), getLatestError());

    request = createHttpRequest("", "");
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    assertNull(getLatestError());
    sos = (MockServletOutputStream) response.getOutputStream();
    String result = sos.getContentsAsString();

    // Redirected so not rendered. Result should be empty string
    assertEquals("", result);

    setRequestParameter("output", null);
    setRequestParameter("critics", "show");
    setRequestParameter("skin", "TestReposSkin2");
    request = createHttpRequest(contextName, "/main/Fourth.md");
    response = createHttpResponse();
    thothServlet.doPost(request, response);
    sos = (MockServletOutputStream) response.getOutputStream();
    result = sos.getContentsAsString();
    assertTrue(result.indexOf("Skinned by TESTREPOSSKIN2") != -1);
    assertTrue(result.indexOf("Yes there is a title!</title>") != -1);
  }

  @Test
  public void testError() throws ServletException, IOException, ContextNotFoundException, ContentManagerException {

    String contextName = "TestContext";
    String path = "/main/Fourth.md";
    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    createTestContentManager(thothEnvironment, contextName);

    HttpServletRequest request = createHttpRequest(contextName, path);
    HttpServletResponse response = createHttpResponse();

    ThothServlet thothServlet = new ThothServlet();
    thothServlet.setThothContext(thothEnvironment);
    thothServlet.init();

    thothServlet.registerCommand(getFailingCommand());
    setRequestParameter("cmd", "fail");
    thothServlet.doGet(request, response);
    MockServletOutputStream sos = (MockServletOutputStream) response.getOutputStream();
    String result = sos.getContentsAsString();
    assertTrue(result.indexOf("Thoth is very sorry about this") != -1);
  }

  @Test
  public void testJson() throws ServletException, IOException, ContextNotFoundException, ContentManagerException {

    String contextName = "TestContext";
    String path = "/";
    ThothEnvironment thothEnvironment = createThothTestEnvironment(contextName);
    createTestContentManager(thothEnvironment, contextName);

    setRequestParameter("mode", "json");
    HttpServletRequest request = createHttpRequest(contextName, path);
    HttpServletResponse response = createHttpResponse();

    ThothServlet thothServlet = new ThothServlet();
    thothServlet.setThothContext(thothEnvironment);
    thothServlet.init();

    thothServlet.doGet(request, response);
    MockServletOutputStream sos = (MockServletOutputStream) response.getOutputStream();
    String result = sos.getContentsAsString();
    assertTrue(result.indexOf("\"skinbase\":\"/TestContext/TestContext/library/TestReposSkin\"") != -1);
  }

  protected Command getFailingCommand() {
    return new Command() {

      @Override
      public String getTypeCode() {
        return "fail";
      }

      @Override
      public String getContentType(Map<String, Object> arguments) {
        return "fail";
      }

      @Override
      public RenderResult execute(Identity identity, String context, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin,
          OutputStream outputStream) throws RenderException {
        throw new RenderException("I was only meant to fail so don't blame me");
      }
    };
  }

}
