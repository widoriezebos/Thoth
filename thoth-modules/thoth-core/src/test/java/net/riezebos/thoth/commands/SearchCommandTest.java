package net.riezebos.thoth.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.commands.util.TestSearchCommand;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class SearchCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Search results", "Sorry, no documents found for your query"};
    String[] jsonExists = new String[] {"\"path\":\"/main/Main.md\""};

    Map<String, String> args = new HashMap<>();
    args.put("query", "Main.md");

    testCommand(new TestSearchCommand(setupContentManager(), this), "/main/Main.md", "search", htmlExists, jsonExists, args);
  }

}
