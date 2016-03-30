package net.riezebos.thoth.server.commands;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import net.riezebos.thoth.configuration.ThothEnvironment;

public class LogLevelServerCommand extends ServerCommand {

  public LogLevelServerCommand(ThothEnvironment thothEnvironment) {
    super(thothEnvironment);
  }

  @Override
  public String getName() {
    return "loglevel";
  }

  @Override
  protected int getNumberOfArguments() {
    return 1;
  }

  @Override
  public void execute(String commandLine) throws Exception {
    String level = getArgument(commandLine, 1);

    switch (level.toLowerCase()) {

    case "debug":
      LogManager.getRootLogger().setLevel(Level.DEBUG);
      println("Logger level set to DEBUG");
      break;

    case "info":
      LogManager.getRootLogger().setLevel(Level.INFO);
      println("Logger level set to INFO");
      break;

    case "warn":
      println("Logger level set to WARN");
      LogManager.getRootLogger().setLevel(Level.WARN);
      break;

    default:
      println("Unrecognized level: " + level);
      println("Use one of DEBUG, INFO, WARN");
    }
  }

  @Override
  protected String[] getArguments() {
    return new String[] {"level"};
  }

  @Override
  public String getDescription() {
    return "Changes the log level of the logging mechanism. Use one of DEBUG, INFO, WARN";
  }
}
