package net.riezebos.thoth.commands;

import java.io.IOException;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class BrowseCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"\"/TestContext/skins.properties\""};
    String[] jsonExists = new String[] {"\"path\":\"/skins.properties\""};

    testCommand(new BrowseCommand(setupContentManager(), this), "/", "browse", htmlExists, jsonExists);
  }
}
