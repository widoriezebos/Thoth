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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.util.ThothUtil;

public class SqlStatement {
  final static String seps = ",<>%* ()-+=[]{};:'\"/|\t\r\n";
  private static final Logger LOG = LoggerFactory.getLogger(SqlStatement.class);

  private Connection connection;
  private PreparedStatement statement;
  private String originalStmt;
  private String sql;
  private String parsedSql;
  private List<String> parameters = new ArrayList<String>();
  private List<String> singleParams = new ArrayList<String>();
  private ResultSetMetaData metaData = null;
  private ResultSet resultSet = null;
  private HashMap<String, Object> parameterValues = new HashMap<String, Object>();
  private int timeOutTime = 0;
  private boolean commitAllowed = true;
  private boolean batchupdateEnabled = false;
  private int maxBatchSize = 0;
  private int currentBatchSize = 0;

  public SqlStatement(Connection connection, String stmt) throws SQLException {
    setConnection(connection);
    setSql(stmt);
    setOriginalStmt(stmt);
  }

  private void setSql(String p_newSql) throws SQLException {
    sql = p_newSql;

    setParsedSql(p_newSql);

    String work = p_newSql + " ";

    char[] ca = work.toCharArray();
    char[] finalStmt = p_newSql.toCharArray();

    int offset = 1;
    int len = work.length();
    boolean inText = false;
    boolean inAlias = false;
    int inc;

    // Wipe all (static) 'text' to make sure that textual colons like 'Here:
    // blabla' don't
    // mess up retrieval of parameters later on

    while (offset < len) {
      inc = 1;
      if (ca[offset] == '\'') {
        if (!inText && !inAlias)
          inText = true;
        else if (inText) {
          if (ca[offset + 1] != '\'') {
            ca[offset] = ' ';
            inText = false;
          } else
            inc = 2;
          // Skip quotes in text i.e. 'some quotes: '' in text'
        }
      } else if ((ca[offset] == '"') && !inText) {
        inAlias = !inAlias;
        ca[offset] = ' ';
      }

      if (inText || inAlias) {
        ca[offset] = ' ';
        if (inc != 1)
          ca[offset + 1] = ' ';
      }
      if (!inText && !inAlias) {
        offset = work.indexOf('\'', offset + inc);
        if (offset == -1)
          offset = len;
      } else
        offset += inc;
    }
    work = new String(ca);

    getParameters().clear();

    int start = 1;
    do {
      offset = work.indexOf(':', start);
      // skip '::' which is valid in postgres
      while (offset > 0 && work.charAt(offset + 1) == ':') {
        offset = work.indexOf(':', offset + 2);
      }
      if (offset > 0) {
        finalStmt[offset] = '?';

        start = offset + 1;
        int end = start;

        while ((end < len) && (seps.indexOf(work.charAt(end)) == -1))
          end++;
        for (int i = start; i < end; i++) {
          finalStmt[i] = ' ';
        }
        String parName = work.substring(start, end);

        getParameters().add(parName);
        boolean dontAdd = false;
        for (int x = 0; x < getSingleParams().size(); x++) {
          if (((String) (getSingleParams().get(x))).equalsIgnoreCase(parName)) {
            dontAdd = true;
            break;
          }
        }
        if (!dontAdd)
          getSingleParams().add(parName);
      }
    } while (offset >= 0);

    setParsedSql(new String(finalStmt));

    if (getParsedSql().endsWith(";")) {
      setParsedSql(getParsedSql().substring(0, getParsedSql().length() - 1));
    }
    try {
      if (getStatement() != null) {
        try {
          getStatement().close();
        } catch (SQLException sss) {
          sss.printStackTrace();
        }
      }
      setStatement(getConnection().prepareStatement(getParsedSql()));
    } catch (SQLException s1qle) {
      // DjLogger.warn(Messages.getString("SqlStatement.errorInStatement", _parsedSql));
      throw s1qle;
    }

    setMetaData(null);
    setResultSet(null);
  }

  public void setCommitAllowed(boolean b) {
    commitAllowed = b;
  }

  public boolean isCommitAllowed() {
    return commitAllowed;
  }

  public String fancyStmt() {
    return fancyStmt(getSql());
  }

  public String fancyStmt(String stmt) {
    final String seps = " ,.()+-'";

    if (stmt == null)
      return "No statement";

    stmt = stmt.trim();
    stmt = stmt.replace("\r\n", " ");
    stmt = stmt.replace("\n", " ");
    stmt = stmt.replace("\t", " ");
    stmt = ThothUtil.replaceWord(stmt, "insert", "\ninsert", seps);
    stmt = ThothUtil.replaceWord(stmt, "values", "\nvalues", seps);
    stmt = ThothUtil.replaceWord(stmt, "update", "\nupdate", seps);
    stmt = ThothUtil.replaceWord(stmt, "delete", "\ndelete", seps);
    stmt = ThothUtil.replaceWord(stmt, "select", "\nselect", seps);
    stmt = ThothUtil.replaceWord(stmt, "from", "\nfrom", seps);
    stmt = ThothUtil.replaceWord(stmt, "where", "\nwhere", seps);
    stmt = ThothUtil.replaceWord(stmt, "and", "\nand", seps);
    stmt = ThothUtil.replaceWord(stmt, "or", "\nor", seps);
    stmt = ThothUtil.replaceWord(stmt, "union", "\nunion", seps);
    stmt = ThothUtil.replaceWord(stmt, "minus", "\nminus", seps);
    stmt = ThothUtil.replaceWord(stmt, "order", "\norder", seps);
    stmt = stmt.replace(",     ", ",\n    ");

    return (stmt.trim());
  }

  public int[] executeBatch() throws SQLException {
    return getStatement().executeBatch();
  }

  public int flushBatch() throws SQLException {
    if (isBatchupdateEnabled()) {
      if (getCurrentBatchSize() > 0) {
        setCurrentBatchSize(getMaxBatchSize() + 1);
        return executeUpdate();
      }
    }
    return 0;
  }

  public int executeUpdate() throws SQLException {
    LOG.debug("Execute:\n" + getOriginalStmt());

    // First the special cases
    String test = getSql();
    if (test.length() > 20) {
      test = getSql().substring(0, 20).trim().toLowerCase();
    }

    if (test.startsWith("commit")) {
      if (!isCommitAllowed())
        throw new SQLException("Commit not allowed");
      getStatement().executeBatch();
      getConnection().commit();
      return 0;
    }
    if (test.startsWith("rollback")) {
      getConnection().rollback();
      return 0;
    }

    try {
      if (isBatchupdateEnabled()) {
        if (getCurrentBatchSize() >= getMaxBatchSize()) {
          int c[] = getStatement().executeBatch();
          setCurrentBatchSize(0);
          int total = 0;
          for (int i = 0; i < c.length; i++) {
            total += c[i];
          }
          return total;
        } else {
          getStatement().addBatch();
          setCurrentBatchSize(getCurrentBatchSize() + 1);
          return 1;
        }
      } else {
        return getStatement().executeUpdate();
      }
    } catch (SQLException x) {
      // This might be just a warning: no records hit.
      // In this case ignore the exception and return 0
      // for 0 records hit.
      if ("02000".equals(x.getSQLState()))
        return 0;

      throw createError(x);
    }
  }

  private SQLException createError(SQLException x) {
    return new SQLException(x.getMessage() + "\n" + fancyStmt(getSql()), x.getSQLState(), x.getErrorCode());
  }

  public ResultSet executeQuery() throws SQLException {
    LOG.debug("executeQuery" + getOriginalStmt());

    try {
      setResultSet(getStatement().executeQuery());
      return (getResultSet());
    } catch (SQLException x) {
      throw createError(x);
    }
  }

  public void close() throws SQLException {
    getStatement().close();
  }

  // void setAsciiStream(int parameterIndex, java.io.InputStream x, int length)
  // throws SQLException;
  public void setAsciiStream(String paramName, InputStream ips, int len) throws SQLException {
    storeParameterValue(paramName, ips);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setAsciiStream(i, ips, len);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setBigDecimal(String paramName, BigDecimal b) throws SQLException {
    storeParameterValue(paramName, b);
    LOG.debug(paramName + "=" + b);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setBigDecimal(i, b);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setBinaryStream(String paramName, InputStream ips, int s) throws SQLException {
    storeParameterValue(paramName, ips);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setBinaryStream(i, ips, s);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setBoolean(String paramName, boolean b) throws SQLException {
    storeParameterValue(paramName, new Boolean(b));
    LOG.debug(paramName + "=" + b);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setBoolean(i, b);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setByte(String paramName, byte b) throws SQLException {
    storeParameterValue(paramName, new Byte(b));
    LOG.debug(paramName + "=" + b);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setByte(i, b);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setBytes(String paramName, byte[] b) throws SQLException {
    storeParameterValue(paramName, b);
    LOG.debug(paramName + "=" + b);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setBytes(i, b);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setDate(String paramName, java.util.Date d) throws SQLException {
    if (d == null)
      setNull(paramName, Types.DATE);
    else
      setDate(paramName, new java.sql.Date(d.getTime()));
  }

  public void setDate(String paramName, java.sql.Date d) throws SQLException {
    storeParameterValue(paramName, d);
    LOG.debug(paramName + "=" + new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(d));

    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setDate(i, d);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setDouble(String paramName, double d) throws SQLException {
    storeParameterValue(paramName, new Double(d));
    LOG.debug(paramName + "=" + d);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setDouble(i, d);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setFloat(String paramName, float f) throws SQLException {
    storeParameterValue(paramName, new Float(f));
    LOG.debug(paramName + "=" + f);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setFloat(i, f);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setInt(String paramName, int n) throws SQLException {
    storeParameterValue(paramName, Integer.valueOf(n));
    LOG.debug(paramName + "=" + n);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setInt(i, n);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setLong(String paramName, long l) throws SQLException {
    storeParameterValue(paramName, new Long(l));
    LOG.debug(paramName + "=" + l);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setLong(i, l);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setNull(String paramName, int sqlType) throws SQLException {
    getParameterValues().remove(paramName.toLowerCase());
    LOG.debug(paramName + "= NULL");
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setNull(i, sqlType);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setObject(String paramName, Object o) throws SQLException {
    storeParameterValue(paramName, o);
    LOG.debug(paramName + "=(Object)" + o);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setObject(i, o);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setObject(String paramName, Object o, int targetSQLType) throws SQLException {
    storeParameterValue(paramName, o);
    LOG.debug(paramName + "=(Object)" + o);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setObject(i, o, targetSQLType);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setObject(String paramName, Object o, int targetSQLType, int scale) throws SQLException {
    storeParameterValue(paramName, o);
    LOG.debug(paramName + "=(Object)" + o);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setObject(i, o, targetSQLType, scale);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setShort(String paramName, short s) throws SQLException {
    storeParameterValue(paramName, new Short(s));
    LOG.debug(paramName + "=" + s);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setShort(i, s);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void set(String paramName, Object v) throws SQLException {
    if (v == null)
      setString(paramName, null);
    else if (v instanceof String)
      setString(paramName, (String) v);
    else if (v instanceof Integer)
      setInt(paramName, ((Integer) v).intValue());
    else if (v instanceof Long)
      setLong(paramName, ((Long) v).longValue());
    else if (v instanceof BigDecimal)
      setBigDecimal(paramName, (BigDecimal) v);
    else if (v instanceof Double)
      setDouble(paramName, ((Double) v).doubleValue());
    else if (v instanceof Short)
      setDouble(paramName, ((Short) v).shortValue());
    else if (v instanceof Float)
      setFloat(paramName, ((Float) v).floatValue());
    else if (v instanceof Boolean)
      setBoolean(paramName, ((Boolean) v).booleanValue());
    else if (v instanceof Date)
      setDate(paramName, (Date) v);
    else if (v instanceof Time)
      setTime(paramName, (Time) v);
    else if (v instanceof Timestamp)
      setTimestamp(paramName, (Timestamp) v);
    else if (v instanceof byte[])
      setBytes(paramName, (byte[]) v);
    else
      throw new SQLException("Unsupported parameter type: " + v.getClass().getName());

  }

  public void setString(String paramName, String s) throws SQLException {
    storeParameterValue(paramName, s);
    LOG.debug(paramName + "=" + s);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        if (s == null)
          getStatement().setNull(i, java.sql.Types.VARCHAR);
        else
          getStatement().setString(i, s);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setBlob(String paramName, String s, String ecodingMethod) throws SQLException {
    storeParameterValue(paramName, s);
    try {
      setBlob(paramName, s.getBytes(ecodingMethod));
    } catch (UnsupportedEncodingException uee) {
      System.out.println(uee);
      setBlob(paramName, s.getBytes());
    }
  }

  public void setBlob(String paramName, byte[] bytes) throws SQLException {
    storeParameterValue(paramName, bytes);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        if (bytes == null)
          getStatement().setNull(i, java.sql.Types.BLOB);
        else {
          ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
          getStatement().setBinaryStream(i, bais, bytes.length);
        }
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setTime(String paramName, Time t) throws SQLException {
    storeParameterValue(paramName, t);
    LOG.debug(paramName + "=" + t);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setTime(i, t);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  public void setTimestamp(String paramName, Date date) throws SQLException {
    Timestamp t = null;
    if (date != null)
      t = new Timestamp(date.getTime());
    setTimestamp(paramName, t);
  }

  public void setTimestamp(String paramName, Timestamp t) throws SQLException {
    storeParameterValue(paramName, t);
    LOG.debug(paramName + "=" + t);
    int i = 1;
    boolean doneOne = false;
    Iterator<String> it = getParameters().iterator();
    while (it.hasNext()) {
      if (it.next().equalsIgnoreCase(paramName)) {
        getStatement().setTimestamp(i, t);
        doneOne = true;
      }
      i++;
    }
    if (!doneOne)
      throw new SQLException("Parameter " + paramName + " not found in " + fancyStmt());
  }

  private void storeParameterValue(String paramName, Object value) {
    getParameterValues().put(paramName.toLowerCase(), value);
  }

  public java.sql.PreparedStatement getStmt() {
    return getStatement();
  }

  public int getParamCount() {
    return getSingleParams().size();
  }

  public List<String> getParameterNames() {
    return getSingleParams();
  }

  public String getParameter(int zeroBaseIndex) {
    return getSingleParams().get(zeroBaseIndex);
  }

  public boolean hasParameter(String paramName) {
    return getSingleParams().contains(paramName.toUpperCase());
  }

  protected void checkMetaData() throws SQLException {
    if (getResultSet() == null)
      throw new SQLException("No MetaData");
    if (getMetaData() == null) {
      setMetaData(getResultSet().getMetaData());
    }
  }

  public int getPropertyIndex(String columnName) throws SQLException {
    checkMetaData();
    int colCount = getMetaData().getColumnCount();

    for (int i = 1; i <= colCount; i++) {
      if (getMetaData().getColumnName(i).equalsIgnoreCase(columnName)) {
        return (i);
      }
    }
    throw new SQLException("Column not in resultset: " + columnName);
  }

  public int getPropertyCount() throws SQLException {
    checkMetaData();
    return (getMetaData().getColumnCount());
  }

  public String getPropertyName(int oneBasedPropertyIndex) throws SQLException {
    checkMetaData();
    return (getMetaData().getColumnName(oneBasedPropertyIndex));
  }

  public static byte[] getBlob(ResultSet rs, String columnName) throws SQLException, IOException {
    InputStream bis = rs.getBinaryStream(columnName);
    return processBlob(bis);
  }

  public static byte[] getBlob(ResultSet rs, int columnIdx) throws SQLException, IOException {
    InputStream bis = rs.getBinaryStream(columnIdx);

    return processBlob(bis);
  }

  private static byte[] processBlob(InputStream bis) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buff = new byte[4096];

    if (bis == null)
      return null;
    for (;;) {
      int size = bis.read(buff);
      if (size == -1)
        break;
      bos.write(buff, 0, size);
    }
    return bos.toByteArray();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(500);
    sb.append(fancyStmt());
    Iterator<String> it = getParameters().iterator();
    sb.append("\n");
    while (it.hasNext()) {
      String paramName = it.next().toString();
      sb.append(":");
      sb.append(paramName.toLowerCase());
      sb.append("=");
      sb.append(getParamValue(paramName));
      sb.append("\n");
    }
    return sb.toString();
  }

  public Object getParamValue(String paramName) {
    return getParameterValues().get(paramName.toLowerCase());
  }

  public boolean isBatchupdateEnabled() {
    return batchupdateEnabled;
  }

  public void setBatchupdateEnabled(boolean batchupdateEnabled) {
    this.batchupdateEnabled = batchupdateEnabled;
  }

  public int getMaxBatchSize() {
    return maxBatchSize;
  }

  public void setMaxBatchSize(int maxBatchSize) {
    this.maxBatchSize = maxBatchSize;
  }

  private int getCurrentBatchSize() {
    return currentBatchSize;
  }

  private void setCurrentBatchSize(int currentBatchSize) {
    this.currentBatchSize = currentBatchSize;
  }

  protected int getTimeOutTime() {
    return timeOutTime;
  }

  protected void setTimeOutTime(int timeOutTime) {
    this.timeOutTime = timeOutTime;
  }

  protected String getParsedSql() {
    return parsedSql;
  }

  protected void setParsedSql(String parsedSql) {
    this.parsedSql = parsedSql;
  }

  protected String getSql() {
    return sql;
  }

  protected String getOriginalStmt() {
    return originalStmt;
  }

  protected void setOriginalStmt(String originalStmt) {
    this.originalStmt = originalStmt;
  }

  protected PreparedStatement getStatement() {
    return statement;
  }

  protected void setStatement(PreparedStatement statement) {
    this.statement = statement;
  }

  protected Connection getConnection() {
    return connection;
  }

  protected void setConnection(Connection connection) {
    this.connection = connection;
  }

  protected List<String> getParameters() {
    return parameters;
  }

  protected void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }

  protected List<String> getSingleParams() {
    return singleParams;
  }

  protected void setSingleParams(List<String> singleParams) {
    this.singleParams = singleParams;
  }

  protected ResultSetMetaData getMetaData() {
    return metaData;
  }

  protected void setMetaData(ResultSetMetaData metaData) {
    this.metaData = metaData;
  }

  protected ResultSet getResultSet() {
    return resultSet;
  }

  protected void setResultSet(ResultSet resultSet) {
    this.resultSet = resultSet;
  }

  protected HashMap<String, Object> getParameterValues() {
    return parameterValues;
  }

  protected void setParameterValues(HashMap<String, Object> parameterValues) {
    this.parameterValues = parameterValues;
  }

  public String getInternalSql() {
    return getSql();
  }

  public String getExternalSql() {
    return getOriginalStmt();
  }

}
