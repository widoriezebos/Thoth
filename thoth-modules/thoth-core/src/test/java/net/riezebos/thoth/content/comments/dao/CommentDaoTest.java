package net.riezebos.thoth.content.comments.dao;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.content.comments.Comment;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.testutil.DatabaseTest;
import net.riezebos.thoth.util.ThothUtil;

public class CommentDaoTest extends DatabaseTest {

  @Test
  public void test() throws IOException, ConfigurationException, SQLException, ContentManagerException {
    try {
      ThothDB thothDB = getThothDB();
      CommentDao dao = new CommentDao(thothDB);

      String body = "This is a comment";
      String documentPath = "/some/path";
      String title = "Some title";
      String userName = "Wido";

      Comment comment = new Comment();
      comment.setBody(body);
      comment.setDocumentPath(documentPath);
      comment.setTitle(title);
      comment.setUserName(userName);
      dao.createComment(comment);

      comment.setBody(body + "2");
      comment.setDocumentPath(documentPath + "2");
      comment.setTitle(title + "2");
      comment.setUserName(userName + "2");
      dao.createComment(comment);

      List<Comment> comments = dao.getComments(null, null);
      assertEquals(2, comments.size());

      comments = dao.getComments(documentPath, null);
      assertEquals(1, comments.size());
      Comment check = comments.get(0);
      assertEquals(userName, check.getUserName());
      assertEquals(ThothUtil.stripPrefix(documentPath, "/"), check.getDocumentPath());
      assertEquals(title, check.getTitle());
      assertEquals(body, check.getBody());

      comments = dao.getComments("invalid", null);
      assertEquals(0, comments.size());

      comments = dao.getComments(null, userName);
      assertEquals(1, comments.size());

      comments = dao.getComments(null, "invalid");
      assertEquals(0, comments.size());

      comments = dao.getComments(documentPath, userName);
      assertEquals(1, comments.size());

      Long id = comment.getId();
      Comment comment2 = dao.getComment(id);
      assertEquals(title+"2", comment2.getTitle());

      dao.deleteComment(id);
      comment = dao.getComment(id);
      assertNull(comment);

    } finally {
      cleanupTempFolder();
    }
  }
}
