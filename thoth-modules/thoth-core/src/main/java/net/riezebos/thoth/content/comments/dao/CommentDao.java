package net.riezebos.thoth.content.comments.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.configuration.persistence.dbs.SequenceGenerator;
import net.riezebos.thoth.configuration.persistence.dbs.SqlStatement;
import net.riezebos.thoth.content.comments.Comment;
import net.riezebos.thoth.content.comments.CommentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.util.BaseDao;
import net.riezebos.thoth.util.ThothUtil;

public class CommentDao extends BaseDao implements CommentManager {
  private ThothDB thothDB;

  public CommentDao(ThothDB thothDB) {
    this.thothDB = thothDB;
  }

  @Override
  public Comment createComment(Comment comment) throws ContentManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement commentStmt = new SqlStatement(connection, thothDB.getQuery("insert_comment"));
        SqlStatement commentBodyStmt = new SqlStatement(connection, thothDB.getQuery("insert_commentbody"))) {

      SequenceGenerator sequenceGenerator = new SequenceGenerator(connection, Tables.THOTH_COMMENTS);
      long id = sequenceGenerator.getNextValue();
      commentStmt.setLong("id", id);
      commentStmt.setString("username", comment.getUserName());
      commentStmt.setString("contextname", comment.getContextName());
      commentStmt.setString("documentpath", comment.getDocumentPath());
      commentStmt.setString("title", comment.getTitle());
      commentStmt.setTimestamp("timecreated", comment.getTimeCreated());
      commentStmt.executeUpdate();

      commentBodyStmt.setLong("comm_id", id);
      commentBodyStmt.setString("commentbody", comment.getBody());
      commentBodyStmt.executeUpdate();

      comment.setId(id);
      commitReload(connection);
      return comment;
    } catch (SQLException | DatabaseException e) {
      throw new ContentManagerException(e);
    }
  }

  @Override
  public boolean deleteComment(long id) throws ContentManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement commentStmt = new SqlStatement(connection, thothDB.getQuery("delete_comment"));
        SqlStatement commentBodyStmt = new SqlStatement(connection, thothDB.getQuery("delete_commentbody"))) {

      commentBodyStmt.setLong("comm_id", id);
      commentBodyStmt.executeUpdate();

      commentStmt.setLong("id", id);
      int count = commentStmt.executeUpdate();

      commitReload(connection);
      return count == 1;
    } catch (SQLException e) {
      throw new ContentManagerException(e);
    }
  }

  @Override
  public boolean deleteComment(Comment comment) throws ContentManagerException {
    return deleteComment(comment.getId());
  }

  @Override
  public List<Comment> getComments(String contextName, String documentpath, String userName) throws ContentManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement commentStmt = constructCommentQuery(connection, contextName, ThothUtil.stripPrefix(documentpath, "/"), userName)) {

      List<Comment> result = new ArrayList<>();

      try (ResultSet rs = commentStmt.executeQuery()) {
        while (rs.next()) {
          Comment comment = new Comment();
          int idx = 1;
          comment.setId(rs.getLong(idx++));
          comment.setUserName(rs.getString(idx++));
          comment.setContextName(rs.getString(idx++));
          comment.setDocumentPath(rs.getString(idx++));
          comment.setTimeCreated(rs.getTimestamp(idx++));
          comment.setTitle(rs.getString(idx++));
          comment.setDao(this);
          result.add(comment);
        }
      }
      return result;
    } catch (SQLException e) {
      throw new ContentManagerException(e);
    }
  }

  @Override
  public Comment getComment(long id) throws ContentManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement commentStmt = new SqlStatement(connection, thothDB.getQuery("select_comment"))) {

      commentStmt.setLong("id", id);

      Comment comment = null;
      try (ResultSet rs = commentStmt.executeQuery()) {
        while (rs.next()) {
          comment = new Comment();
          int idx = 1;
          comment.setId(id);
          comment.setUserName(rs.getString(idx++));
          comment.setContextName(rs.getString(idx++));
          comment.setDocumentPath(rs.getString(idx++));
          comment.setTimeCreated(rs.getTimestamp(idx++));
          comment.setTitle(rs.getString(idx++));
          comment.setDao(this);
        }
      }
      return comment;
    } catch (SQLException e) {
      throw new ContentManagerException(e);
    }
  }

  protected SqlStatement constructCommentQuery(Connection connection, String contextName, String documentpath, String userName) throws SQLException {
    String query = thothDB.getQuery("select_comments");
    String where = "";

    if (StringUtils.isNotBlank(contextName)) {
      where += " contextname = :contextname ";
    }
    if (StringUtils.isNotBlank(documentpath)) {
      if (StringUtils.isNotBlank(where))
        where += " and ";
      where += " documentpath = :documentpath ";
    }
    if (userName != null) {
      if (StringUtils.isNotBlank(where))
        where += " and ";
      where += " username = :username ";
    }
    if (StringUtils.isNotBlank(where))
      query += " where " + where;

    query += " order by timecreated";

    SqlStatement sqlStatement = new SqlStatement(connection, query);
    if (StringUtils.isNotBlank(contextName)) {
      sqlStatement.setString("contextname", contextName);
    }
    if (StringUtils.isNotBlank(documentpath)) {
      sqlStatement.setString("documentpath", documentpath);
    }
    if (userName != null) {
      sqlStatement.setString("username", userName);
    }

    return sqlStatement;
  }

  public String getBody(Long id) throws ContentManagerException {
    try (Connection connection = thothDB.getConnection(); //
        SqlStatement commentStmt = new SqlStatement(connection, thothDB.getQuery("select_commentbody"))) {
      commentStmt.setLong("comm_id", id);
      String result = "";
      try (ResultSet rs = commentStmt.executeQuery()) {
        if (rs.next()) {
          result = rs.getString(1);
        }
      }
      return result;
    } catch (SQLException e) {
      throw new ContentManagerException(e);
    }
  }
}