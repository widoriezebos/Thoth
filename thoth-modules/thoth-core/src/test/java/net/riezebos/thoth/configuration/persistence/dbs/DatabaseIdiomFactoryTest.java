package net.riezebos.thoth.configuration.persistence.dbs;

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
