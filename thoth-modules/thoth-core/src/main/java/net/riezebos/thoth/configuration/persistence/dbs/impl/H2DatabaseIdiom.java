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

public class H2DatabaseIdiom extends GenericDatabaseIdiom {

  public H2DatabaseIdiom() {
    registerType(Types.BOOLEAN, "boolean");
    registerType(Types.BIGINT, "bigint");
    registerType(Types.BINARY, "binary");
    registerType(Types.BIT, "boolean");
    registerType(Types.CHAR, "char($l)");
    registerType(Types.DATE, "date");
    registerType(Types.DECIMAL, "decimal($p,$s)");
    registerType(Types.NUMERIC, "decimal($p,$s)");
    registerType(Types.DOUBLE, "double");
    registerType(Types.FLOAT, "float");
    registerType(Types.INTEGER, "integer");
    registerType(Types.LONGVARBINARY, "longvarbinary");
    registerType(Types.LONGVARCHAR, "longvarchar");
    registerType(Types.REAL, "real");
    registerType(Types.SMALLINT, "smallint");
    registerType(Types.TINYINT, "tinyint");
    registerType(Types.TIME, "time");
    registerType(Types.TIMESTAMP, "timestamp");
    registerType(Types.VARCHAR, "varchar($l)");
    registerType(Types.VARBINARY, "binary($l)");
    registerType(Types.BLOB, "blob");
    registerType(Types.CLOB, "clob");
  }

  @Override
  public void initDialect(List<TranslationPair> translations, List<TranslationPair> workarounds) {
    super.initDialect(translations, workarounds);

    workarounds.add(new TranslationPair("create\\s*sequence\\s*(\\w+)\\s*start\\s*\\s*with\\s*(\\d+)\\s*minvalue\\s*(\\d+)\\s*maxvalue\\s*(\\d+)",
        "create sequence if not exists $1 start with $2 minvalue $3 maxvalue $4"));

  }

  @Override
  public DatabaseFlavour getFlavour() {
    return DatabaseFlavour.H2;
  }

  @Override
  public String getTableName(String tableName) {
    return tableName.toUpperCase();
  }

}
