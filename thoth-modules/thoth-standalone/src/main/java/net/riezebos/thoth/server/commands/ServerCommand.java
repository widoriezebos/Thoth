/* Copyright (c) 2016 W.T.J. Riezebos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
