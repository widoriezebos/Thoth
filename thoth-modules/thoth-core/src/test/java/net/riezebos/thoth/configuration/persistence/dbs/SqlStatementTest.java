package net.riezebos.thoth.configuration.persistence.dbs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.testutil.DatabaseTest;

public class SqlStatementTest extends DatabaseTest {

  @Test
  public void testSqlStatement() throws SQLException, DatabaseException, IOException, ConfigurationException {

    ThothDB thothDB = getThothDB();

    try (Connection connection = thothDB.getConnection()) {
      SqlStatement stmt =
          new SqlStatement(connection, "select ':test' \"alias\", id, firstname, lastname from thoth_users where id <> :id+2 and id <> :id + 1;");
      assertEquals(1, stmt.getParamCount());
      stmt.setLong("id", 400);
      ResultSet rs = stmt.executeQuery();
      assertTrue(rs.next());
      stmt.close();
      String fancyStmt = stmt.fancyStmt();
      SqlStatement stmt2 = new SqlStatement(connection, fancyStmt);
      assertEquals(1, stmt2.getParamCount());
      stmt2.setBigDecimal("id", new BigDecimal(100));
      stmt2.close();

      stmt = new SqlStatement(connection, "update thoth_users set firstname = 'test';");
      int count = stmt.executeUpdate();
      assertTrue(count > 0);
      stmt.close();

    } finally {
      cleanupTempFolder();
    }
  }
}
