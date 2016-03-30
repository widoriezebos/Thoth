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

public class OracleDatabaseIdiom extends GenericDatabaseIdiom {

  public OracleDatabaseIdiom() {
    registerType(Types.VARCHAR, "varchar2($l char)");
    registerType(Types.CHAR, "char(1)");
    registerType(Types.BIT, "number(1,0)");
    registerType(Types.BIGINT, "number(19,0)");
    registerType(Types.SMALLINT, "number(5,0)");
    registerType(Types.TINYINT, "number(3,0)");
    registerType(Types.INTEGER, "number(10,0)");
    registerType(Types.FLOAT, "float");
    registerType(Types.DOUBLE, "double precision");
    registerType(Types.NUMERIC, "number($p,$s)");
    registerType(Types.DECIMAL, "number($p,$s)");
    registerType(Types.BOOLEAN, "number(1,0)");
    registerType(Types.DATE, "date");
    registerType(Types.TIME, "date");
    registerType(Types.TIMESTAMP, "date");
    registerType(Types.BINARY, "long raw");
    registerType(Types.VARBINARY, "long raw");
    registerType(Types.BLOB, "blob");
    registerType(Types.CLOB, "clob");
    registerType(Types.LONGVARCHAR, "long");
    registerType(Types.LONGVARBINARY, "long raw");
  }

  @Override
  public String getCurrentTimeDDL() {
    return "sysdate";
  }

  @Override
  public DatabaseFlavour getFlavour() {
    return DatabaseFlavour.ORACLE;
  }

  @Override
  public void initDialect(List<TranslationPair> translations, List<TranslationPair> workarounds) {
    translations.add(new TranslationPair("timestamp", "date", "timestamp date,select"));

    workarounds.add(new TranslationPair("alter\\s*table\\s*(\\w+)\\s*alter\\s*column\\s*(\\w+)\\s*drop\\s*not\\s*null", "alter table $1 modify ($2 null)"));
    workarounds.add(new TranslationPair("long varchar2", "long varchar"));
    workarounds.add(new TranslationPair("clob\\s*\\(\\s*\\d*\\s*\\)", "clob"));
    workarounds.add(new TranslationPair("blob\\s*\\(\\s*\\d*\\s*\\)", BLOB));
    workarounds.add(new TranslationPair("bool(\\s|\\s*,)", "varchar(1)$1"));
    workarounds.add(new TranslationPair("bytea", BLOB));
    workarounds.add(new TranslationPair("varchar\\s*\\(\\s*\\d*\\s*\\) for bit data", "long varchar"));
    workarounds.add(new TranslationPair("\\s*add\\s+column\\s*", " add "));
    workarounds.add(new TranslationPair("\\s*alter\\s+column\\s*", " modify "));
    workarounds.add(new TranslationPair("alter\\s+table\\s+(\\w+)\\s*alter\\s+column", "alter table $1 modify "));
    workarounds.add(new TranslationPair("alter\\s+table\\s+(\\w+)\\s*alter\\s", "alter table $1 modify "));
    workarounds.add(new TranslationPair("modify\\s+(\\w+)\\s+type\\s", "modify $1 "));
    workarounds.add(new TranslationPair("modify\\s+(\\w+)\\s+set\\s", "modify $1 "));
    workarounds.add(new TranslationPair("varchar2?\\s*\\(\\s*(\\d*)\\s*\\)", "varchar2($1 CHAR) "));
    workarounds.add(new TranslationPair("substring\\((.*?)\\Wfrom\\W(\\d+)\\Wfor\\W(\\d+)\\)", "substr($1, $2, $3)"));
    workarounds.add(new TranslationPair("rename\\s*table\\s*(\\w+)\\s*to\\s*(\\w+)", "rename $1 to $2"));
  }

  @Override
  public String getTableName(String tableName) {
    return tableName.toUpperCase();
  }

  @Override
  public String getCurrentTime() {
    return "(select sysdate from dual)";
  }

  @Override
  public String getDatabaseEntity(String name) {
    if (name.length() > 30)
      name = name.substring(0, 30);
    return name.toUpperCase();
  }
}
