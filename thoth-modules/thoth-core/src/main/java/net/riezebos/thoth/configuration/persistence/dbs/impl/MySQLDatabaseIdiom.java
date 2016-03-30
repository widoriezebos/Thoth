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
package net.riezebos.thoth.configuration.persistence.dbs.impl;

import java.sql.Types;
import java.util.List;
import java.util.regex.Pattern;

public class MySQLDatabaseIdiom extends GenericDatabaseIdiom {

  public MySQLDatabaseIdiom() {
    registerType(Types.VARCHAR, "varchar($l)");
    registerType(Types.LONGVARCHAR, "longtext");
    registerType(Types.BIT, "bit");
    registerType(Types.BIGINT, "bigint");
    registerType(Types.SMALLINT, "smallint");
    registerType(Types.TINYINT, "tinyint");
    registerType(Types.INTEGER, "integer");
    registerType(Types.CHAR, "char(1)");
    registerType(Types.FLOAT, "float");
    registerType(Types.DOUBLE, "double precision");
    registerType(Types.DATE, "date");
    registerType(Types.TIME, "time");
    registerType(Types.TIMESTAMP, "datetime");
    registerType(Types.VARBINARY, "longblob");
    registerType(Types.BINARY, "binary($l)");
    registerType(Types.LONGVARBINARY, "longblob");
    registerType(Types.NUMERIC, "decimal($p,$s)");
    registerType(Types.BLOB, "longblob");
    registerType(Types.CLOB, "longtext");
  }

  @Override
  public String getCurrentTimeDDL() {
    return "current_timestamp";
  }

  @Override
  public String applyDialect(String command) {
    // The following modification is absolutely VITAL because if the engine is ISAM there is no support
    // for transactions i.e. a failing transaction is committed causing a corrupt database!
    Pattern pattern = Pattern.compile("create\\s+table\\s.*");
    if (pattern.matcher(command.toLowerCase()).matches()) {
      command += " ENGINE=InnoDB";
    }
    return command;
  }

  @Override
  public DatabaseFlavour getFlavour() {
    return DatabaseFlavour.MYSQL;
  }

  @Override
  public void initDialect(List<TranslationPair> translations, List<TranslationPair> workarounds) {
    workarounds.add(new TranslationPair("long varchar2", "long varchar"));
    workarounds.add(new TranslationPair("clob\\s*\\(\\s*\\d*\\s*\\)", "clob"));
    workarounds.add(new TranslationPair("blob\\s*\\(\\s*\\d*\\s*\\)", BLOB));
    workarounds.add(new TranslationPair("bytea", BLOB));
    workarounds.add(new TranslationPair("longtext\\s*\\(\\s*\\d*\\s*\\)", "longtext"));
    workarounds.add(new TranslationPair("varchar\\s*\\(\\s*\\d*\\s*\\) for bit data", BLOB));
  }
}
