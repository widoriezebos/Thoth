package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class RevisionsCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Latest changes"};
    String[] jsonExists = new String[] {"\"path\":\"/main/Main.md\""};

    testCommand(new RevisionsCommand(), "/main/Main.md", "revisions", htmlExists, jsonExists);
  }

}
