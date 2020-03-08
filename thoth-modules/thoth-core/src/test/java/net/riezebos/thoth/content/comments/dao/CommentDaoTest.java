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
      String contextName = "ctxtName";
      String documentPath = "/some/path";
      String title = "Some title";
      String userName = "Wido";

      Comment comment = new Comment();
      comment.setBody(body);
      comment.setContextName(contextName);
      comment.setDocumentPath(documentPath);
      comment.setTitle(title);
      comment.setUserName(userName);
      dao.createComment(comment);

      comment.setBody(body + "2");
      comment.setContextName(contextName);
      comment.setDocumentPath(documentPath + "2");
      comment.setTitle(title + "2");
      comment.setUserName(userName + "2");
      dao.createComment(comment);

      List<Comment> comments = dao.getComments(null, null, null);
      assertEquals(2, comments.size());

      comments = dao.getComments(contextName, documentPath, null);
      assertEquals(1, comments.size());
      Comment check = comments.get(0);
      assertEquals(userName, check.getUserName());
      assertEquals(ThothUtil.stripPrefix(documentPath, "/"), check.getDocumentPath());
      assertEquals(title, check.getTitle());
      assertEquals(body, check.getBody());

      comments = dao.getComments(contextName, "invalid", null);
      assertEquals(0, comments.size());

      comments = dao.getComments(null, null, userName);
      assertEquals(1, comments.size());

      comments = dao.getComments(null, null, "invalid");
      assertEquals(0, comments.size());

      comments = dao.getComments(contextName, documentPath, userName);
      assertEquals(1, comments.size());

      Long id = comment.getId();
      Comment comment2 = dao.getComment(id);
      assertEquals(title + "2", comment2.getTitle());

      dao.deleteComment(id);
      comment = dao.getComment(id);
      assertNull(comment);

    } finally {
      cleanupTempFolder();
    }
  }
}
