package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class IndexCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Documentation Index", "TestContext"};
    String[] jsonExists = new String[] {"\"context\":\"TestContext\""};

    testCommand(new IndexCommand(setupContentManager(), this), "/", "index", htmlExists, jsonExists);
  }

}
