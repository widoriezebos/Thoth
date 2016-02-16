package net.riezebos.thoth.servlets;

import static org.junit.Assert.assertEquals;

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
    HttpServletRequest request = getRequest(contextName, path);
    HttpServletResponse response = getResponse();

    setRequestParameter("output", "raw");
    setRequestParameter("cmd", "null");
    setRequestParameter("skin", "null");

    ThothServlet thothServlet = new ThothServlet();
    thothServlet.setConfiguration(contentManager.getConfiguration());
    thothServlet.init();

    thothServlet.doGet(request, response);
    MockServletOutputStream sos = (MockServletOutputStream) response.getOutputStream();
    String actual = sos.getContentsAsString().trim();
    String expected = getExpected("Fourth.expected.md");
    assertEquals(expected, actual);
  }

}
