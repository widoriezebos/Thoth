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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.persistence.dbs.impl.DDLException;
import net.riezebos.thoth.configuration.persistence.dbs.impl.TranslationPair;
import net.riezebos.thoth.util.OutputListener;
import net.riezebos.thoth.util.ThothUtil;

/**
 * Executes a given database script; including any additional scripts if they exist. The script itself may not be executed because of the following rules Given
 * script resources/script.ddl Suppose the database is derby, the following scripts will be executed in this order (if they exist): resources/script.pre.ddl
 * resources/script.derby.pre.ddl resources/script.derby.ddl resources/script.custom.ddl resources/script.post.ddl resources/script.derby.post.ddl Note that the
 * script resources/script.derby.ddl is executed and resources/script.ddl is not because a database specific script overrides the default
 **/
public class DDLExecuter {

  private static final Logger LOG = LoggerFactory.getLogger(DDLExecuter.class);
  private static final String SEPARATORS = "(), \t\n\r':;.<>*/-+=[]{}?";
  private final List<TranslationPair> translations = new ArrayList<TranslationPair>();
  private final List<TranslationPair> workarounds = new ArrayList<TranslationPair>();
  private final List<OutputListener> listeners = new ArrayList<OutputListener>();
  private DatabaseIdiom databaseIdiom;
  private Connection connection = null;

  public DDLExecuter(Connection connection, DatabaseIdiom idiom) {
    this.connection = connection;
    initDialect(idiom);
  }

  public void execute(String resourceLocation) throws DDLException, IOException {
    Connection conn = getConnection();
    try {
      println("Executing script " + resourceLocation);
      String script = readResource(resourceLocation);
      StringBuffer commandBuffer = stripMultiLineComment(new StringBuffer(script));

      List<String> cmds = parseCommands(commandBuffer);
      try (Statement stmt = conn.createStatement()) {
        for (String command : cmds) {
          executeSingleStmt(conn, stmt, command);
        }
        conn.commit();
      }
    } catch (SQLException sqle) {
      throw new DDLException(sqle);
    }
  }

  protected void executeSingleStmt(Connection conn, Statement stmt, String command) throws DDLException {
    String actual = command;
    try {
      String dialect = applyDialect(command);
      println(dialect);
      actual = dialect;
      if (StringUtils.isNotBlank(dialect))
        stmt.execute(dialect);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      throw new DDLException("Error executing " + actual + "\n" + e.toString());
    }
  }

  public boolean tableExists(String tableName, String schemaName) throws SQLException {
    DatabaseMetaData md = getConnection().getMetaData();
    tableName = databaseIdiom.getTableName(tableName);
    try (ResultSet rs = md.getTables(null, null, tableName, new String[] {"TABLE"})) {
      return rs.next();
    }
  }

  protected String applyDialect(String command) {
    String timestampRe = "\\{fn CURRENT_TIMESTAMP\\}";
    command = command.replaceAll(timestampRe, databaseIdiom.getCurrentTimeDDL());

    // Only translate create/alter statements
    boolean createOrAlter = command.toLowerCase().startsWith("create") || command.toLowerCase().startsWith("alter")
        || command.toLowerCase().startsWith("rename") || command.toLowerCase().startsWith("drop");
    if (!createOrAlter)
      return command;

    for (TranslationPair wa : translations) {
      if (!wa.exclude(command)) {
        command = ThothUtil.replaceWord(command + " ", wa.getFrom(), wa.getTo(), SEPARATORS).trim();
      }
    }

    for (TranslationPair wa : workarounds) {
      Pattern pattern = Pattern.compile(wa.getFrom(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
      command = pattern.matcher(command).replaceAll(wa.getTo());
    }

    command = databaseIdiom.applyDialect(command);
    return command;
  }

  private List<String> parseCommands(StringBuffer commandBuffer) {
    String[] parsed = commandBuffer.toString().split(";");
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < parsed.length; i++) {
      String trimmedCmd = trimCmd(parsed[i]);
      if (!StringUtils.isBlank(trimmedCmd))
        result.add(trimmedCmd);
    }

    return result;
  }

  private String trimCmd(String lines) {
    StringBuilder cmd = new StringBuilder();
    for (String line : lines.trim().split("\n")) {
      line = line.trim();
      if (line.length() > 0 && !line.startsWith("//") && !line.startsWith("--"))
        cmd.append(line + "\n");
    }
    String trimmedCmd = cmd.toString().trim();
    return trimmedCmd;
  }

  private StringBuffer stripMultiLineComment(StringBuffer commandBuffer) {
    int beginComment = commandBuffer.indexOf("/*", 0);
    int endComment = 0;
    while (beginComment != -1) {
      endComment = commandBuffer.indexOf("*/", beginComment);
      commandBuffer = commandBuffer.delete(beginComment, endComment + 2);
      beginComment = commandBuffer.indexOf("/*");
    }
    return commandBuffer;
  }

  protected Connection getConnection() {
    return connection;
  }

  private void initDialect(DatabaseIdiom idiom) {
    databaseIdiom = idiom;
    try {
      addTranslation("bigint", idiom.getTypeName(Types.BIGINT));
      addTranslation("binary", idiom.getTypeName(Types.BINARY));
      addTranslation("bit", idiom.getTypeName(Types.BIT));
      addTranslation("blob", idiom.getTypeName(Types.BLOB));
      addTranslation("boolean", idiom.getTypeName(Types.BOOLEAN));
      addTranslation("char", idiom.getTypeName(Types.CHAR));
      addTranslation("clob", idiom.getTypeName(Types.CLOB));
      addTranslation("date", idiom.getTypeName(Types.DATE));
      addTranslation("decimal", idiom.getTypeName(Types.DECIMAL));
      addTranslation("double", idiom.getTypeName(Types.DOUBLE));
      addTranslation("float", idiom.getTypeName(Types.FLOAT));
      addTranslation("integer", idiom.getTypeName(Types.INTEGER));
      addTranslation("longnvarchar", idiom.getTypeName(Types.LONGNVARCHAR));
      addTranslation("longvarbinary", idiom.getTypeName(Types.LONGVARBINARY));
      addTranslation("longvarchar", idiom.getTypeName(Types.LONGVARCHAR));
      addTranslation("nchar", idiom.getTypeName(Types.NCHAR));
      addTranslation("nclob", idiom.getTypeName(Types.NCLOB));
      addTranslation("numeric", idiom.getTypeName(Types.NUMERIC));
      addTranslation("nvarchar", idiom.getTypeName(Types.NVARCHAR));
      addTranslation("real", idiom.getTypeName(Types.REAL));
      addTranslation("smallint", idiom.getTypeName(Types.SMALLINT));
      addTranslation("time", idiom.getTypeName(Types.TIME));
      addTranslation("timestamp", idiom.getTypeName(Types.TIMESTAMP));
      addTranslation("tinyint", idiom.getTypeName(Types.TINYINT));
      addTranslation("varbinary", idiom.getTypeName(Types.VARBINARY));

      databaseIdiom.initDialect(translations, workarounds);
      // Ignore autogenerated ID's
      addWorkaround("generated always as identity", "");
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected void addWorkaround(String from, String to) {
    workarounds.add(new TranslationPair(from, to));
  }

  protected void addTranslation(String name, String translatedType) {
    if (translatedType != null) {
      // If there is a precision specified as a parameter i.e. decimal->number($p,$s) then strip the ($p,$s) part
      if (translatedType.indexOf('$') != -1) {
        int idx = translatedType.indexOf('(');
        if (idx != -1)
          translatedType = translatedType.substring(0, idx);
      }
      if (!translatedType.equalsIgnoreCase(name)) {
        trace("Added dialect translation " + name + "->" + translatedType);
        translations.add(new TranslationPair(name, translatedType));
      }
    } else {
      System.out.println(name + " -> null");
    }
  }

  public void addOutputListener(OutputListener lsnr) {
    listeners.add(lsnr);
  }

  public void println(String msg) {
    LOG.debug(msg);
    for (OutputListener lsnr : listeners)
      lsnr.println(msg);
  }

  private void trace(String msg) {
    LOG.debug(msg);
    for (OutputListener lsnr : listeners)
      lsnr.trace(msg);
  }

  protected String readResource(String name) throws IOException {
    InputStream resourceAsStream = getResourceAsStream(name);
    return ThothUtil.readInputStream(resourceAsStream);
  }

  protected InputStream getResourceAsStream(String name) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
  }
}
