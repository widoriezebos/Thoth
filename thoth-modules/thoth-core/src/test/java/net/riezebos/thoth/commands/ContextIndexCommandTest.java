package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class ContextIndexCommandTest extends CommandTest {
  String contextName = "TestContext";

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Books by Category", "TestContext/books/Main.book"};
    String[] jsonExists = new String[] {"\"name\":\"Second.book\""};

    testCommand(new ContextIndexCommand(),  "/", "contextindex", htmlExists, jsonExists);
  }

}
