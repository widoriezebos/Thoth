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
package net.riezebos.thoth.content.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManagerBase;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.util.PagedList;

/**
 * NOP content manager. Will serve nothing; used as the content manager for the main index
 *
 * @author wido
 */
public class NopContentManager extends ContentManagerBase {

  public NopContentManager(ContextDefinition contextDefinition, ThothEnvironment context) throws ContentManagerException {
    super(contextDefinition, context);
    setFileSystem(new ClasspathFileSystem("non/existing/class/root/so/will/not/serve/anything"));
  }

  /**
   * Will not pull because this is a simple file system. However this method will do change detection by using a simple checksum strategy (not based on contents
   * but on modification dates and filenames) When a change is detected an indexer will be launched by calling notifyContextContentsChanged(). When called for
   * the first time (i.e. previous checksum was 0) it will always notifyContextContentsChanged()
   */
  @Override
  protected synchronized String cloneOrPull() throws ContentManagerException {

    setLatestRefresh(new Date());
    return getContextName() + ": NOP. Will do nothing";
  }

  @Override
  public PagedList<Commit> getCommits(String path, int pageNumber, int pageSize) throws ContentManagerException {
    return new PagedList<>(new ArrayList<>(), false);

  }

  @Override
  public SourceDiff getDiff(String diffSpec) throws ContentManagerException {

    return new SourceDiff("nobody", "", "", new Date(0L));
  }

  protected void validateContextDefinition(ContextDefinition contextDefinition) throws ContentManagerException {
  }

  @Override
  public boolean supportsVersionControl() {
    return false;
  }

  public InputStream getInputStream(String path) throws IOException {
    return null;
  }

  @Override
  public List<ContentNode> list(String path) throws ContextNotFoundException, IOException {
    return new ArrayList<>();
  }

  @Override
  public List<ContentNode> find(String fileSpec, boolean recursive) throws IOException {
    return new ArrayList<>();
  }

  @Override
  protected void notifyContextContentsChanged() {
  }

  @Override
  public void reindex() {
  }
}
