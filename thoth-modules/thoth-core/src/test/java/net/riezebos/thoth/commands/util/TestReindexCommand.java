package net.riezebos.thoth.commands.util;

import net.riezebos.thoth.commands.ReindexCommand;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.renderers.RendererProvider;

public class TestReindexCommand extends ReindexCommand {

  public TestReindexCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  protected void reindex(ContentManager contentManager) {
    // Do nothing
  }

}
