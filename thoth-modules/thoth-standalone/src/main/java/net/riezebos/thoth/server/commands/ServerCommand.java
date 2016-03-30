package net.riezebos.thoth.server.commands;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.configuration.ThothEnvironment;

public abstract class ServerCommand {
  private ThothEnvironment thothEnvironment;

  public abstract String getName();

  public abstract String getDescription();

  public abstract void execute(String commandLine) throws Exception;

  public ServerCommand(ThothEnvironment thothEnvironment) {
    this.thothEnvironment = thothEnvironment;
  }

  public ThothEnvironment getThothEnvironment() {
    return thothEnvironment;
  }

  public boolean applies(String commandLine) {
    if (StringUtils.isBlank(commandLine))
      return false;
    return commandLine.toLowerCase().startsWith(getName());
  }

  public boolean argumentsOk(String commandLine) {
    return commandLine.split("\\s").length == getNumberOfArguments() + 1;
  }

  protected int getNumberOfArguments() {
    return 0;
  }

  protected String getArgument(String commandLine, int i) {
    String[] args = commandLine.split("\\s");
    return args[i];
  }

  public void println(String message) {
    System.out.println(message);
  }

  public String getUsage() {
    String result = getName();
    for (String argName : getArguments())
      result += " <" + argName + ">";
    return result;
  }

  protected String[] getArguments() {
    return new String[0];
  }

}
