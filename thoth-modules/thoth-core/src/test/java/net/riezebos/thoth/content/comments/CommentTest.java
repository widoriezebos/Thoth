package net.riezebos.thoth.content.comments;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommentTest {

  @Test
  public void test() {
    String repeat = "1234567890";
    
    Comment comment = new Comment();
    
    String body = "";
    for(int i=0; i<20; i++) {
      body += repeat;
    }
    comment.setBody(body);
    
    assertTrue(comment.getTitle().startsWith(repeat));
    assertTrue(comment.getTitle().length() <= Comment.MAX_DEDUCE_TITLE_LENGTH);
    assertTrue(comment.getTitle().endsWith("..."));
    
  }

}
