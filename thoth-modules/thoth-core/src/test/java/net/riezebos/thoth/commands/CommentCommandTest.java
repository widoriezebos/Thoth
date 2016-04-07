package net.riezebos.thoth.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.comments.Comment;
import net.riezebos.thoth.content.comments.CommentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.renderers.RenderResult;

public class CommentCommandTest extends CommandTest {

  @Test
  public void test()
      throws ContextNotFoundException, SkinManagerException, RenderException, UnsupportedEncodingException, ContentManagerException, IOException {

    ThothEnvironment thothEnvironment = setupContentManager();
    CommentManager commentManager = thothEnvironment.getCommentManager();
    CommentCommand commentCommand = new CommentCommand(thothEnvironment, this);
    Map<String, String> args = new HashMap<String, String>();
    RenderResult renderResult = testCommand(commentCommand, "/", CommandOperation.GET, "comment", null, null, args);
    assertEquals(RenderResult.OK, renderResult);

    String commentBody = "This is a comment";

    // CREATE COMMENT
    String[] jsonExists = new String[] {};
    args = new HashMap<String, String>();
    args.put(CommentCommand.OPERATION_ARGUMENT, CommentCommand.CREATE);
    args.put(CommentCommand.COMMENTTEXT_ARGUMENT, commentBody);
    args.put(CommentCommand.DOCPATH_ARGUMENT, "/some/path");
    renderResult = testCommand(commentCommand, "/", CommandOperation.POST, "comment", null, jsonExists, args);

    List<Comment> comments = commentManager.getComments(TEST_CONTEXT_NAME, "/some/path", null);
    assertTrue(comments.size() == 1);
    Comment comment = comments.get(0);

    // COPY COMMENT
    jsonExists = new String[] {commentBody};
    args = new HashMap<String, String>();
    args.put(CommentCommand.OPERATION_ARGUMENT, CommentCommand.COPY);
    args.put(CommentCommand.COMMENTID_ARGUMENT, String.valueOf(comment.getId()));
    renderResult = testCommand(commentCommand, "/", CommandOperation.POST, "comment", null, jsonExists, args);

    // DELETE COMMENT
    jsonExists = new String[] {};
    args = new HashMap<String, String>();
    args.put(CommentCommand.OPERATION_ARGUMENT, CommentCommand.DELETE);
    args.put(CommentCommand.COMMENTID_ARGUMENT, String.valueOf(comment.getId()));
    renderResult = testCommand(commentCommand, "/", CommandOperation.POST, "comment", null, jsonExists, args);
    comments = commentManager.getComments(TEST_CONTEXT_NAME, "/some/path", null);
    assertTrue(comments.size() == 0);

  }

}
