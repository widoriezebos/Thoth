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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiom;

public class GenericDatabaseIdiom implements DatabaseIdiom {

  protected static final String BLOB = "blob";
  private Map<Integer, String> typeNames = new HashMap<Integer, String>();

  public GenericDatabaseIdiom() {
    registerType(Types.BIT, "bit");
    registerType(Types.BOOLEAN, "boolean");
    registerType(Types.TINYINT, "tinyint");
    registerType(Types.SMALLINT, "smallint");
    registerType(Types.INTEGER, "integer");
    registerType(Types.BIGINT, "bigint");
    registerType(Types.FLOAT, "float($p)");
    registerType(Types.DOUBLE, "double precision");
    registerType(Types.NUMERIC, "numeric($p,$s)");
    registerType(Types.DECIMAL, "numeric($p,$s)");
    registerType(Types.REAL, "real");
    registerType(Types.DATE, "date");
    registerType(Types.TIME, "time");
    registerType(Types.TIMESTAMP, "timestamp");
    registerType(Types.VARBINARY, "bit varying($l)");
    registerType(Types.LONGVARBINARY, "bit varying($l)");
    registerType(Types.BLOB, "blob");
    registerType(Types.CHAR, "char($l)");
    registerType(Types.VARCHAR, "varchar($l)");
    registerType(Types.LONGVARCHAR, "varchar($l)");
    registerType(Types.CLOB, "clob");
    registerType(Types.NCHAR, "nchar($l)");
    registerType(Types.NVARCHAR, "nvarchar($l)");
    registerType(Types.LONGNVARCHAR, "nvarchar($l)");
    registerType(Types.NCLOB, "nclob");
  }

  protected void registerType(int code, String name) {
    typeNames.put(code, name);
  }

  @Override
  public String getTableName(String tableName) {
    return tableName;
  }

  @Override
  public String getCurrentTimeDDL() {
    return getCurrentTime();
  }

  @Override
  public String applyDialect(String command) {
    return command;
  }

  @Override
  public void initDialect(List<TranslationPair> translations, List<TranslationPair> workarounds) {
  }

  @Override
  public DatabaseFlavour getFlavour() {
    return DatabaseFlavour.OTHER;
  }

  @Override
  public String getCurrentTime() {
    return "now()";
  }

  @Override
  public String getDatabaseEntity(String name) {
    return name;
  }

  @Override
  public Map<String, String> getTranslations() {
    return new HashMap<>();
  }

  @Override
  public String getDefaultIndexType() {
    return "";
  }

  @Override
  public String getTypeName(int type) {
    return typeNames.get(type);
  }

}
