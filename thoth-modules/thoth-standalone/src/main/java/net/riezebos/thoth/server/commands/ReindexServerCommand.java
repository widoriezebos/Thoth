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
 */package net.riezebos.thoth.server.commands;

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
