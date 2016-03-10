package net.riezebos.thoth.content.comments;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.content.comments.dao.CommentDao;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class Comment {
  private static final Logger LOG = LoggerFactory.getLogger(Comment.class);

  private Long id;
  private String userName;
  private String documentPath;
  private String body;
  private String title;
  private Date timeCreated = new Date();
  private CommentDao commentDao;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDocumentPath() {
    return documentPath;
  }

  public void setDocumentPath(String documentPath) {
    this.documentPath = documentPath;
  }

  public String getBody() {
    if (body == null && commentDao != null) {
      try {
        body = commentDao.getBody(getId());
      } catch (ContentManagerException e) {
        body = e.getMessage();
        LOG.error(e.getMessage(), e);
      }
    }
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Date getTimeCreated() {
    return timeCreated;
  }

  public void setTimeCreated(Date timeCreated) {
    this.timeCreated = timeCreated;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDao(CommentDao commentDao) {
    this.commentDao = commentDao;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

}
