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
package net.riezebos.thoth.configuration;

import org.apache.commons.lang3.StringUtils;

public class ContextDefinition implements Cloneable {

  private RepositoryDefinition repositoryDefinition;
  private String name;
  private String branch;
  private String library;
  private long refreshIntervalMS;

  public ContextDefinition(RepositoryDefinition repositoryDefinition, String name, String branch, String library, long refreshIntervalMS) {
    super();
    this.repositoryDefinition = repositoryDefinition;
    this.name = name;
    this.branch = branch;
    this.refreshIntervalMS = refreshIntervalMS;
    if (StringUtils.isBlank(library))
      library = "";
    this.library = library;
  }

  public RepositoryDefinition getRepositoryDefinition() {
    return repositoryDefinition;
  }

  public String getName() {
    return name;
  }

  public String getBranch() {
    return branch;
  }

  public String getLibrary() {
    return library;
  }

  public long getRefreshIntervalMS() {
    return refreshIntervalMS;
  }

  @Override
  protected ContextDefinition clone() {
    try {
      ContextDefinition clone = (ContextDefinition) super.clone();
      clone.repositoryDefinition = repositoryDefinition.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((branch == null) ? 0 : branch.hashCode());
    result = prime * result + ((library == null) ? 0 : library.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + (int) (refreshIntervalMS ^ (refreshIntervalMS >>> 32));
    result = prime * result + ((repositoryDefinition == null) ? 0 : repositoryDefinition.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ContextDefinition other = (ContextDefinition) obj;
    if (branch == null) {
      if (other.branch != null)
        return false;
    } else if (!branch.equals(other.branch))
      return false;
    if (library == null) {
      if (other.library != null)
        return false;
    } else if (!library.equals(other.library))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (refreshIntervalMS != other.refreshIntervalMS)
      return false;
    if (repositoryDefinition == null) {
      if (other.repositoryDefinition != null)
        return false;
    } else if (!repositoryDefinition.equals(other.repositoryDefinition))
      return false;
    return true;
  }
}
