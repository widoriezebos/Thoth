/* Copyright (c) 2020 W.T.J. Riezebos
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

public class HSQLDatabaseIdiom extends GenericDatabaseIdiom {

  public HSQLDatabaseIdiom() {
    registerType(Types.BIGINT, "bigint");
    registerType(Types.BINARY, "binary($l)");
    registerType(Types.BIT, "bit");
    registerType(Types.BOOLEAN, "boolean");
    registerType(Types.CHAR, "char($l)");
    registerType(Types.DATE, "date");
    registerType(Types.DECIMAL, "decimal($p,$s)");
    registerType(Types.DOUBLE, "double");
    registerType(Types.FLOAT, "float");
    registerType(Types.INTEGER, "integer");
    registerType(Types.LONGVARBINARY, "longvarbinary");
    registerType(Types.LONGVARCHAR, "longvarchar");
    registerType(Types.SMALLINT, "smallint");
    registerType(Types.TINYINT, "tinyint");
    registerType(Types.TIME, "time");
    registerType(Types.TIMESTAMP, "timestamp");
    registerType(Types.VARCHAR, "varchar($l)");
    registerType(Types.VARBINARY, "varbinary($l)");
    registerType(Types.NUMERIC, "numeric($p,$s)");
    registerType(Types.BLOB, "blob($l)");
    registerType(Types.CLOB, "clob($l)");
  }

  @Override
  public DatabaseFlavour getFlavour() {
    return DatabaseFlavour.HSQLDB;
  }

  @Override
  public String getTableName(String tableName) {
    return tableName.toUpperCase();
  }
}
