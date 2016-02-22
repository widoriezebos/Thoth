package net.riezebos.thoth.commands.util;

import net.riezebos.thoth.commands.ReindexCommand;
import net.riezebos.thoth.configuration.ThothContext;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class TestReindexCommand extends ReindexCommand {

  public TestReindexCommand(ThothContext thothContext) {
    super(thothContext);
  }

  @Override
  protected void reindex(String context) throws ContentManagerException {
    // Do nothing
  }

}
