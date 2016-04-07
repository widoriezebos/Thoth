package net.riezebos.thoth.content.comments;

import java.util.List;

import net.riezebos.thoth.exceptions.ContentManagerException;

public interface CommentManager {

  Comment createComment(Comment comment) throws ContentManagerException;

  boolean deleteComment(long id) throws ContentManagerException;

  boolean deleteComment(Comment comment) throws ContentManagerException;

  List<Comment> getComments(String contextName, String documentpath, String userName) throws ContentManagerException;

  Comment getComment(long id) throws ContentManagerException;

}
