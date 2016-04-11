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
 */package net.riezebos.thoth.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.commands.util.CommandTest;
import net.riezebos.thoth.commands.util.TestSearchCommand;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;

public class SearchCommandTest extends CommandTest {

  @Test
  public void test() throws ContextNotFoundException, ContentManagerException, IOException {

    String[] htmlExists = new String[] {"Search results", "Sorry, no documents found for your query"};
    String[] jsonExists = new String[] {"\"path\":\"/main/Main.md\""};

    Map<String, String> args = new HashMap<>();
    args.put("query", "Main.md");

    testCommand(new TestSearchCommand(setupContentManager(), this), "/main/Main.md", CommandOperation.GET, "search", htmlExists, jsonExists, args);
  }

}
