/* Copyright (c) 2016 W.T.J. Riezebos
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
package net.riezebos.thoth.configuration.persistence.dbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.riezebos.thoth.exceptions.DatabaseException;

public class SequenceGenerator {
  private static final int SLEEP_TIME = 500;
  private static final int RETRIES = 3;
  private long initialValue;
  private int increment;
  private String sequenceName;
  private Connection connection;

  public SequenceGenerator(Connection connection, String sequenceName) {
    this(connection, sequenceName, 1, 1);
  }

  public SequenceGenerator(Connection connection, String sequenceName, long initialValue, int increment) {
    this.initialValue = initialValue;
    this.increment = increment;
    this.sequenceName = sequenceName;
    this.connection = connection;
  }

  /**
   * Resets the sequence to the initial value and it does this within the current transaction of the EntityManager
   *
   * @param sequenceName
   * @throws DatabaseException
   */
  public void resetSequence() throws DatabaseException {
    try {
      PreparedStatement qry = connection.prepareStatement("update thoth_sequences set next_val = ? where sequence_name = ?");
      qry.setLong(1, initialValue);
      qry.setString(2, sequenceName);
      qry.executeUpdate();
      qry.close();
      connection.commit();
    } catch (SQLException sqle) {
      throw new DatabaseException(sqle);
    }
  }

  /**
   * Returns the next value of the sequence; without blocking other transactions. NOTE this method does not guarantee the absence of gaps in the numbering!
   * Sessions that fetch a number but do a rollback will cause gaps in the numbering of the sequence.
   *
   * @param sequenceName
   * @return
   * @throws DatabaseException
   */
  public long getNextValue() throws DatabaseException {
    int retries = RETRIES;
    do {
      try {
        return doGetNextValue(sequenceName);
      } catch (Exception x) {
        if (--retries < 0)
          throw new DatabaseException(x);
        try {
          Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
          // do nothing
        }
      }
    } while (true);
  }

  protected long doGetNextValue(String sequenceName) throws DatabaseException {

    long result;
    try {
      PreparedStatement lockQry = connection.prepareStatement("select next_val from thoth_sequences where sequence_name = ? for update");
      lockQry.setString(1, sequenceName);
      ResultSet executeQuery = lockQry.executeQuery();
      boolean locked = executeQuery.next();
      executeQuery.close();
      lockQry.close();

      if (!locked) {
        PreparedStatement qry2 = connection.prepareStatement("insert into thoth_sequences(sequence_name, next_val) values(?, ?)");
        qry2.setString(1, sequenceName);
        qry2.setLong(2, initialValue + increment);
        qry2.executeUpdate();
        qry2.close();
      } else {
        PreparedStatement qry2 = connection.prepareStatement("update thoth_sequences set next_val = next_val + ? where sequence_name = ?");
        qry2.setLong(1, increment);
        qry2.setString(2, sequenceName);
        qry2.executeUpdate();
        qry2.close();
      }

      PreparedStatement qry = connection.prepareStatement("select next_val from thoth_sequences where sequence_name = ?");
      qry.setString(1, sequenceName);

      ResultSet rs = qry.executeQuery();
      if (rs.next()) {
        result = rs.getLong(1) - increment;
      } else
        throw new DatabaseException("No result");
      rs.close();
      qry.close();
      connection.commit();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
    return result;
  }

  public boolean sequenceExists(String name) throws DatabaseException {
    try {
      PreparedStatement qry = connection.prepareStatement("select 1 from thoth_sequences where sequence_name = ?");
      qry.setString(1, name);
      ResultSet rs = qry.executeQuery();
      boolean result = rs.next();
      rs.close();
      qry.close();
      return result;
    } catch (Exception x) {
      throw new DatabaseException(x);
    }
  }
}
