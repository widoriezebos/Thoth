package net.riezebos.thoth.servlets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.testutil.MockServletOutputStream;
import net.riezebos.thoth.testutil.ThothTestBase;

public class ThothServletTest extends ThothTestBase {

  @Test
  public void test() throws ServletException, IOException, ContextNotFoundException, ContentManagerException {

    String contextName = "TestContext";
    String path = "/main/Fourth.md";

    ContentManager contentManager = registerTestContentManager(contextName);
    HttpServletRequest request = createHttpRequest(contextName, path);
    HttpServletResponse response = createHttpResponse();

    setRequestParameter("output", "raw");

    ThothServlet thothServlet = new ThothServlet();
    thothServlet.setConfiguration(contentManager.getConfiguration());
    thothServlet.init();

    thothServlet.doGet(request, response);
    MockServletOutputStream sos = (MockServletOutputStream) response.getOutputStream();
    String actual = sos.getContentsAsString().trim();
    String expected = getExpected("Fourth.expected.md");
    assertEquals(expected, actual);
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

    request = createHttpRequest(contextName, "");
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    assertNull(getLatestError());
    sos = (MockServletOutputStream) response.getOutputStream();
    String result = sos.getContentsAsString();
    assertTrue(result.indexOf("Books by folder") != -1);
    assertTrue(result.indexOf("TestContext/books/Main.book") != -1);

    request = createHttpRequest("", "");
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    assertNull(getLatestError());
    sos = (MockServletOutputStream) response.getOutputStream();
    result = sos.getContentsAsString();
    assertTrue(result.indexOf("Please select one of the contexts") != -1);
    assertTrue(result.indexOf("TestContext</a>") != -1);

    request = createHttpRequest(contextName, "/main/Fourth.md");
    setRequestParameter("output", null);
    setRequestParameter("skin", "TestReposSkin2");
    response = createHttpResponse();
    thothServlet.doGet(request, response);
    sos = (MockServletOutputStream) response.getOutputStream();
    result = sos.getContentsAsString();
    assertTrue(result.indexOf("Skinned by TESTREPOSSKIN2") != -1);
    assertTrue(result.indexOf("Yes there is a title!</title>") != -1);
  }

}
