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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgreSqlDatabaseIdiom extends GenericDatabaseIdiom {

  public PostgreSqlDatabaseIdiom() {
    registerType(Types.BIT, "bool");
    registerType(Types.BIGINT, "int8");
    registerType(Types.SMALLINT, "int2");
    registerType(Types.TINYINT, "int2");
    registerType(Types.INTEGER, "int4");
    registerType(Types.CHAR, "char(1)");
    registerType(Types.VARCHAR, "varchar($l)");
    registerType(Types.FLOAT, "float4");
    registerType(Types.DOUBLE, "float8");
    registerType(Types.DATE, "date");
    registerType(Types.TIME, "time");
    registerType(Types.TIMESTAMP, "timestamp");
    registerType(Types.VARBINARY, "bytea");
    registerType(Types.BINARY, "bytea");
    registerType(Types.LONGVARCHAR, "text");
    registerType(Types.LONGVARBINARY, "bytea");
    registerType(Types.CLOB, "text");
    registerType(Types.BLOB, "oid");
    registerType(Types.NUMERIC, "numeric($p, $s)");
    registerType(Types.OTHER, "uuid");
  }

  @Override
  public String getCurrentTimeDDL() {
    return "current_timestamp";
  }

  @Override
  public DatabaseFlavour getFlavour() {
    return DatabaseFlavour.POSTGRES;
  }

  @Override
  public void initDialect(List<TranslationPair> translations, List<TranslationPair> workarounds) {
    workarounds.add(new TranslationPair("oid\\s*\\(\\s*\\d*\\s*\\)", "oid"));
    workarounds.add(new TranslationPair("blob\\s*\\(\\s*\\d*\\s*\\)", "oid"));
    workarounds.add(new TranslationPair("varchar\\s*\\(\\s*\\d*\\s*\\) for bit data", "oid"));
    workarounds.add(new TranslationPair("cascade\\s*constraints", "cascade"));
    workarounds.add(new TranslationPair("rename\\s*table\\s*(\\w+)\\s*to\\s*(\\w+)", "alter table $1 rename to $2"));
    workarounds.add(new TranslationPair("create\\s*sequence\\s*(\\w+)\\s*start\\s*\\s*with\\s*(\\d+)\\s*minvalue\\s*(\\d+)\\s*maxvalue\\s*(\\d+)",
        "create sequence $1 minvalue $3 maxvalue $4 start with $2"));
    workarounds.add(new TranslationPair("create\\s*sequence\\s*(.*?)nocycle", "create sequence $1no cycle"));
  }

  @Override
  public String getTableName(String tableName) {
    return tableName.toLowerCase();
  }

  @Override
  public String getCurrentTime() {
    return "now()";
  }

  @Override
  public Map<String, String> getTranslations() {
    Map<String, String> translations = new HashMap<String, String>();
    translations.put("(.*)(\\W)nvl\\((.*?)\\)(.*)", "$1$2coalesce($3)$4");
    translations.put("(.*)(\\W)sysdate(\\W)(.*)", "$1$2now()$3$4");
    return translations;

  }

  @Override
  public String getDefaultIndexType() {
    return " USING btree";
  }
}
