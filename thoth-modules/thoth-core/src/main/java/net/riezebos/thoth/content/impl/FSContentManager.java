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
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.configuration.RepositoryDefinition;
import net.riezebos.thoth.content.ContentManagerBase;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.BasicFileHandle;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.util.PagedList;
import net.riezebos.thoth.util.ThothUtil;

/**
 * Very simple content manager: just get the content from the filesystem; hence no support for collaboration nor synchronization with other servers.
 * 
 * @author wido
 */
public class FSContentManager extends ContentManagerBase {

  private String fsroot;
  private long previousChecksum = 0;

  public FSContentManager(ContextDefinition contextDefinition) throws ContentManagerException {
    super(contextDefinition);
    validateContextDefinition(contextDefinition);
    fsroot = ThothUtil.normalSlashes(contextDefinition.getRepositoryDefinition().getLocation());
    if (!fsroot.endsWith("/"))
      fsroot += "/";
  }

  @Override
  public String getFileSystemPath(String path) throws ContextNotFoundException, IOException {
    path = ThothUtil.stripPrefix(path, "/");
    return fsroot + path;
  }

  @Override
  public String getContextFolder() throws ContextNotFoundException {
    return fsroot;
  }

  /**
   * Will not pull because this is a simple file system. However this method will do change detection by using a simple checksum strategy (not based on contents
   * but on modification dates and filenames) When a change is detected an indexer will be launched by calling notifyContextContentsChanged(). When called for
   * the first time (i.e. previous checksum was 0) it will always notifyContextContentsChanged()
   */
  @Override
  protected synchronized String cloneOrPull() throws ContentManagerException {

    boolean changes = false;
    try {
      long contextChecksum = getContextChecksum();
      changes = contextChecksum != previousChecksum;
      if (changes)
        notifyContextContentsChanged();
      previousChecksum = contextChecksum;
    } catch (IOException e) {
      throw new ContentManagerException(e);
    }

    return getContext() + ": " + (changes ? CHANGES_DETECTED_MSG : NO_CHANGES_DETECTED_MSG);
  }

  @Override
  public PagedList<Commit> getCommits(String path, int pageNumber, int pageSize) throws ContentManagerException {
    return new PagedList<>(new ArrayList<>(), false);

  }

  public SourceDiff getDiff(String diffSpec) throws ContentManagerException {

    return new SourceDiff("nobody", "", "", new Date(0L));
  }

  protected void validateContextDefinition(ContextDefinition contextDefinition) throws ContentManagerException {
    RepositoryDefinition repositoryDefinition = contextDefinition.getRepositoryDefinition();
    if (StringUtils.isBlank(repositoryDefinition.getLocation()))
      throw new ContentManagerException("Location not set for repositiory " + repositoryDefinition.getName());
  }

  @Override
  public boolean supportsVersionControl() {
    return false;
  }

  @Override
  protected FileHandle getFileHandle(String filePath) {
    return new BasicFileHandle(filePath);
  }
}
