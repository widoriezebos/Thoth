package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class MetaCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Document Structure", "/TestContext//main/subs/SubOne.md"};
    String[] jsonExists = new String[] {"\"file\":\"/main/subs/SubOne.md\""};

    testCommand(new MetaCommand(), "/main/Main.md", "meta", htmlExists, jsonExists);
  }

}
