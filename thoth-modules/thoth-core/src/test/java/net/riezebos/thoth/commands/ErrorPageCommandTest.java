package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class ErrorPageCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Thoth is very sorry about this"};
    String[] jsonExists = null;

    testCommand(new ErrorPageCommand(setupContentManager()), "/main/Main.md", "error", htmlExists, jsonExists);
  }

}
