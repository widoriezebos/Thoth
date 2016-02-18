package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class DiffCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Changes to Main"};
    String[] jsonExists = new String[] {"\"path\":\"/main/Main.md\""};

    testCommand(new DiffCommand(), "/main/Main.md", "diff", htmlExists, jsonExists);
  }

}
