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

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManagerBase;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.markdown.filehandle.FileSystem;
import net.riezebos.thoth.markdown.filehandle.ZipFileSystem;
import net.riezebos.thoth.util.PagedList;

/**
 * Very simple content manager: just get the content from a zip; hence no support for collaboration nor synchronization with other servers.
 * 
 * @author wido
 */
public class ZipContentManager extends ContentManagerBase {

  private long previousTimestamp = 0;

  public ZipContentManager(ContextDefinition contextDefinition, ThothEnvironment context) throws ContentManagerException {
    super(contextDefinition, context);
    try {
      ZipFileSystem fileSystem = new ZipFileSystem(contextDefinition.getRepositoryDefinition().getLocation());
      setup(contextDefinition, fileSystem);
    } catch (IOException e) {
      throw new ContentManagerException(e);
    }
  }

  public ZipContentManager(ContextDefinition contextDefinition, ThothEnvironment context, FileSystem fileSystem) throws ContentManagerException {
    super(contextDefinition, context);
    setup(contextDefinition, fileSystem);
  }

  protected void setup(ContextDefinition contextDefinition, FileSystem fileSystem) throws ContentManagerException {
    validateContextDefinition(contextDefinition);
    setFileSystem(fileSystem);
  }

  /**
   * Will not pull because this is a simple file system. However this method will do change detection by looking at the moddate of the zipfile. When a change is
   * detected an indexer will be launched by calling notifyContextContentsChanged(). When called for the first time (i.e. previous checksum was 0) it will
   * always notifyContextContentsChanged()
   */
  @Override
  protected synchronized String cloneOrPull() throws ContentManagerException {

    boolean changes = false;
    ZipFileSystem zipFileSystem = (ZipFileSystem) getFileSystem();
    long latestModification = zipFileSystem.getLatestModification();
    changes = latestModification != previousTimestamp;
    if (changes)
      notifyContextContentsChanged();
    previousTimestamp = latestModification;
    setLatestRefresh(new Date());

    return getContextName() + ": " + (changes ? CHANGES_DETECTED_MSG : NO_CHANGES_DETECTED_MSG);
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
    RepositoryDefinition repositoryDefinition = contextDefinition.getRepositoryDefinition();
    if (StringUtils.isBlank(repositoryDefinition.getLocation()))
      throw new ContentManagerException("Location (of zip file) not set for repositiory " + repositoryDefinition.getName());
  }

  @Override
  public boolean supportsVersionControl() {
    return false;
  }
}
