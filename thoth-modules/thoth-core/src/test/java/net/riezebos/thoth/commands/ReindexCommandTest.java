package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.commands.util.TestReindexCommand;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class ReindexCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Reindex reuested. Running in the background"};
    String[] jsonExists = new String[] {"\"log\":\"Reindex reuested. Running in the background\""};

    testCommand(new TestReindexCommand(setupContentManager()), "/", "reindex", htmlExists, jsonExists);
  }

}
