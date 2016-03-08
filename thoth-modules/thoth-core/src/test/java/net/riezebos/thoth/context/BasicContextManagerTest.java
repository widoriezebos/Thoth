package net.riezebos.thoth.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.configuration.persistence.ThothDB;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContextManagerException;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.testutil.DatabaseTest;

public class BasicContextManagerTest extends DatabaseTest {

  @Test
  public void test() throws DatabaseException, IOException, ConfigurationException, SQLException, ContextManagerException {

    ThothDB thothDB = getThothDB();
    try {
      try (Connection connection = thothDB.getConnection()) {
        ContextManager contextManager = getThothEnvironment().getContextManager();

        Map<String, RepositoryDefinition> repositoryDefinitions = contextManager.getRepositoryDefinitions();
        assertTrue(repositoryDefinitions.isEmpty());
        Map<String, ContextDefinition> contextDefinitions = contextManager.getContextDefinitions();
        assertTrue(contextDefinitions.isEmpty());

        RepositoryDefinition repoDef = new RepositoryDefinition();
        repoDef.setName("name");
        repoDef.setType("git");
        repoDef.setLocation("location");
        repoDef.setUsername("username");
        repoDef.setPassword("password");

        RepositoryDefinition repositoryDefinition = contextManager.createRepositoryDefinition(repoDef);
        RepositoryDefinition repositoryDefinition2 = contextManager.getRepositoryDefinition("name");
        assertEquals(repositoryDefinition, repositoryDefinition2);
        assertFalse(repositoryDefinition == repositoryDefinition2);
        assertEquals("password", repositoryDefinition2.getPassword());

        repositoryDefinition2.setPassword("changed");
        contextManager.updateRepositoryDefinition(repositoryDefinition2);
        repositoryDefinition2 = contextManager.getRepositoryDefinition("name");
        assertEquals("changed", repositoryDefinition2.getPassword());

        ContextDefinition contextDefinition = new ContextDefinition(repositoryDefinition, "contextname", "branch", "libraryRoot", 30000);
        contextManager.createContextDefinition(contextDefinition);

        ContextDefinition contextDefinition2 = contextManager.getContextDefinition("contextname");
        assertEquals("branch", contextDefinition2.getBranch());
        assertEquals(30000, contextDefinition2.getRefreshIntervalMS());

        contextDefinition.setBranch("branch2");
        contextManager.updateContextDefinition(contextDefinition);
        ContextDefinition contextDefinition3 = contextManager.getContextDefinition("contextname");
        assertEquals("branch2", contextDefinition3.getBranch());

        assertTrue(contextManager.isInUse(repoDef));
        assertNotNull(contextManager.getContextDefinitions().get("contextname"));
        contextManager.deleteContextDefinition(contextDefinition3);
        assertNull(contextManager.getContextDefinitions().get("contextname"));
        assertFalse(contextManager.isInUse(repoDef));

        assertNotNull(contextManager.getRepositoryDefinitions().get("name"));
        contextManager.deleteRepositoryDefinition(repoDef);
        assertNull(contextManager.getRepositoryDefinitions().get("name"));
      }
    } finally {
      cleanupTempFolder();
    }
  }

}
