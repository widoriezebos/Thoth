package net.riezebos.thoth.server.commands;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;

public class ReloadServerCommand extends ServerCommand {

  public ReloadServerCommand(ThothEnvironment thothEnvironment) {
    super(thothEnvironment);
  }

  @Override
  public String getName() {
    return "reload";
  }

  @Override
  public void execute(String commandLine) throws Exception {
    Configuration configuration = getThothEnvironment().getConfiguration();
    configuration.reload();
    println("Configuration reloaded from " + configuration.getPropertyFileName());
  }

  @Override
  public String getDescription() {
    return "Reload the configuration file";
  }

}
