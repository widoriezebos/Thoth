package net.riezebos.thoth.server.commands;

import net.riezebos.thoth.configuration.ThothEnvironment;

public class PullServerCommand extends ServerCommand {

  public PullServerCommand(ThothEnvironment thothEnvironment) {
    super(thothEnvironment);
  }

  @Override
  public String getName() {
    return "pull";
  }

  @Override
  public void execute(String commandLine) throws Exception {
    println("Pulling...");
    getThothEnvironment().pullAll();
    println("Done...");
  }

  @Override
  public String getDescription() {
    return "Force a pull of all repositories";
  }
}
