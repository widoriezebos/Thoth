/* Copyright (c) 2020 W.T.J. Riezebos
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
    println("Configuration reloaded from " + configuration.getSourceSpec());
  }

  @Override
  public String getDescription() {
    return "Reload the configuration file";
  }

}
