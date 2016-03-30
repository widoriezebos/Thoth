package net.riezebos.thoth.server.commands;

import net.riezebos.thoth.configuration.ThothEnvironment;

public class ReindexServerCommand extends ServerCommand {

  public ReindexServerCommand(ThothEnvironment thothEnvironment) {
    super(thothEnvironment);
  }

  @Override
  public String getName() {
    return "reindex";
  }

  @Override
  public void execute(String commandLine) throws Exception {
    println("Reindex running in the background");
    getThothEnvironment().reindexAll();
  }

  @Override
  public String getDescription() {
    return "Reindex all contexts";
  }

}
