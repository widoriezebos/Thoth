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
 */package net.riezebos.thoth.configuration.persistence.dbs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.riezebos.thoth.configuration.persistence.dbs.DatabaseIdiom.DatabaseFlavour;

public class DatabaseIdiomFactoryTest {

  @Test
  public void test() {

    assertEquals(DatabaseFlavour.DERBY, DatabaseIdiomFactory.getDatabaseIdiom("derby").getFlavour());
    assertEquals(DatabaseFlavour.H2, DatabaseIdiomFactory.getDatabaseIdiom("h2").getFlavour());
    assertEquals(DatabaseFlavour.HSQLDB, DatabaseIdiomFactory.getDatabaseIdiom("hsqldb").getFlavour());
    assertEquals(DatabaseFlavour.MYSQL, DatabaseIdiomFactory.getDatabaseIdiom("mysql").getFlavour());
    assertEquals(DatabaseFlavour.ORACLE, DatabaseIdiomFactory.getDatabaseIdiom("oracle").getFlavour());
    assertEquals(DatabaseFlavour.OTHER, DatabaseIdiomFactory.getDatabaseIdiom("xyz").getFlavour());
    assertEquals(DatabaseFlavour.POSTGRES, DatabaseIdiomFactory.getDatabaseIdiom("postgress").getFlavour());
    assertEquals(DatabaseFlavour.POSTGRES, DatabaseIdiomFactory.getDatabaseIdiom("postgresql").getFlavour());
  }

}
