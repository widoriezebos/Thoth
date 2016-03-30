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
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.util.PagedList;
import net.riezebos.thoth.util.ThothUtil;

/**
 * Very simple content manager: just get the content from the classpath; hence no support for collaboration nor synchronization with other servers.
 *
 * @author wido
 */
public class ClasspathContentManager extends ContentManagerBase {

  public ClasspathContentManager(ContextDefinition contextDefinition, ThothEnvironment context, ClasspathFileSystem fileSystem) throws ContentManagerException {
    super(contextDefinition, context);
    validateContextDefinition(contextDefinition);
    setFileSystem(fileSystem);
  }

  public ClasspathContentManager(ContextDefinition contextDefinition, ThothEnvironment context) throws ContentManagerException {
    super(contextDefinition, context);
    validateContextDefinition(contextDefinition);
    String fsroot = ThothUtil.normalSlashes(contextDefinition.getRepositoryDefinition().getLocation());
    setFileSystem(new ClasspathFileSystem(fsroot));
  }

  /**
   * Will not pull because this is a simple file system. However this method will do change detection by using a simple checksum strategy (not based on contents
   * but on modification dates and filenames) When a change is detected an indexer will be launched by calling notifyContextContentsChanged(). When called for
   * the first time (i.e. previous checksum was 0) it will always notifyContextContentsChanged()
   */
  @Override
  protected synchronized String cloneOrPull() throws ContentManagerException {

    setLatestRefresh(new Date());
    return getContextName() + ": classpath based. Will do nothing";
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
      throw new ContentManagerException("Location not set for repositiory " + repositoryDefinition.getName());
  }

  @Override
  public boolean supportsVersionControl() {
    return false;
  }

}
