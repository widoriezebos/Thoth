package net.riezebos.thoth.commands.util;

import net.riezebos.thoth.commands.ReindexCommand;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class TestReindexCommand extends ReindexCommand {

  @Override
  protected void reindex(String context) throws ContentManagerException {
    // Do nothing
  }

}
