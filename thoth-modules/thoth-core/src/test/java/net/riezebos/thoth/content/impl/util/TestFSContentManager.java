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
package net.riezebos.thoth.content.impl.util;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.impl.FSContentManager;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class TestFSContentManager extends FSContentManager {

  public TestFSContentManager(ContextDefinition contextDefinition, ThothEnvironment context) throws ContentManagerException {
    super(contextDefinition, context);
  }

  @Override
  protected void notifyContextContentsChanged() {
    // Do not fire an indexer
  }

}
