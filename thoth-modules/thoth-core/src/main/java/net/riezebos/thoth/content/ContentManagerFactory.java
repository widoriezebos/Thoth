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
package net.riezebos.thoth.content;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.content.impl.GitContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class ContentManagerFactory {

  private static ContentManager contentManager = null;

  public static ContentManager getContentManager() throws ContentManagerException {
    synchronized (ContentManagerFactory.class) {
      if (contentManager == null) {
        String type = Configuration.getInstance().getVersionControlType();
        if ("git".equalsIgnoreCase(type))
          contentManager = new GitContentManager();
        else
          throw new ContentManagerException("Unsupported version control type: " + type);
        contentManager.refresh();
        contentManager.enableAutoRefresh();
      }
      return contentManager;
    }
  }
}
