package net.riezebos.thoth.commands.util;

import net.riezebos.thoth.commands.ReindexCommand;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.renderers.RendererProvider;

public class TestReindexCommand extends ReindexCommand {

  public TestReindexCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  protected void reindex(String context) throws ContentManagerException {
    // Do nothing
  }

}
