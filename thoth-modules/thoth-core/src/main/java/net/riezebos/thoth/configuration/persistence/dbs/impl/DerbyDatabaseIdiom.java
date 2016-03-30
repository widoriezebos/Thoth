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

public class DerbyDatabaseIdiom extends GenericDatabaseIdiom {
  @Override
  public String getCurrentTimeDDL() {
    return "{fn TIMESTAMPADD( SQL_TSI_MONTH, 0, CURRENT_TIMESTAMP)}";
  }

  public DerbyDatabaseIdiom() {
    registerType(Types.BOOLEAN, "boolean");
    registerType(Types.BLOB, "blob");
    registerType(Types.BIT, "smallint");
    registerType(Types.BIGINT, "bigint");
    registerType(Types.SMALLINT, "smallint");
    registerType(Types.TINYINT, "smallint");
    registerType(Types.INTEGER, "integer");
    registerType(Types.CHAR, "char(1)");
    registerType(Types.VARCHAR, "varchar($l)");
    registerType(Types.FLOAT, "float");
    registerType(Types.DOUBLE, "double");
    registerType(Types.DATE, "date");
    registerType(Types.TIME, "time");
    registerType(Types.TIMESTAMP, "timestamp");
    registerType(Types.VARBINARY, "varchar($l) for bit data");
    registerType(Types.NUMERIC, "numeric($p,$s)");
    registerType(Types.CLOB, "clob($l)");
    registerType(Types.LONGVARCHAR, "long varchar");
    registerType(Types.LONGVARBINARY, "long varchar for bit data");
    registerType(Types.BINARY, "varchar($l) for bit data");
    registerType(Types.BINARY, "char($l) for bit data");
  }

  @Override
  public DatabaseFlavour getFlavour() {
    return DatabaseFlavour.DERBY;
  }

  @Override
  public void initDialect(List<TranslationPair> translations, List<TranslationPair> workarounds) {
    // Derby doesn't like 'blob(0)'... set it to the default value
    workarounds.add(new TranslationPair("bytea", "blob(10000000)"));
    translations.add(new TranslationPair("blob(0)", "blob(10000000)"));
    workarounds.add(new TranslationPair("bool(\\W)", "varchar(1)$1"));
    workarounds.add(new TranslationPair("create or replace view", "create view"));

    // For now we do not support sequences; but let's not fail on that
    workarounds.add(new TranslationPair("create\\s*sequence\\s*.*", ""));
  }

  @Override
  public String getTableName(String tableName) {
    return tableName.toUpperCase();
  }
}
