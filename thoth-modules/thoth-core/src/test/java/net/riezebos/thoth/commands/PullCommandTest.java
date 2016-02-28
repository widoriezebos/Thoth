package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class PullCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"TestContext: classpath based. Will do nothing"};
    String[] jsonExists = new String[] {"\"log\":\"TestContext: classpath based. Will do nothing"};

    testCommand(new PullCommand(setupContentManager(), this), "/main/Main.md", "pull", htmlExists, jsonExists);
  }
}
