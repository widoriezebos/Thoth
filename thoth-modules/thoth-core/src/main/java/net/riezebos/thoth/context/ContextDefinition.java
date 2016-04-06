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
package net.riezebos.thoth.context;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.riezebos.thoth.util.ThothUtil;

public class ContextDefinition implements Cloneable, Comparable<ContextDefinition> {

  private Long id;
  private RepositoryDefinition repositoryDefinition;
  private String name;
  private String branch;
  private String libraryRoot;
  private long refreshInterval;
  private boolean immutable = false;

  public ContextDefinition(RepositoryDefinition repositoryDefinition, String name, String branch, String libraryRoot, long refreshInterval) {
    super();
    this.repositoryDefinition = repositoryDefinition;
    this.name = name;
    this.branch = branch;
    this.refreshInterval = refreshInterval;
    if (StringUtils.isBlank(libraryRoot))
      libraryRoot = "/";
    this.libraryRoot = ThothUtil.prefix(ThothUtil.stripSuffix(libraryRoot, "/"), "/");
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @JsonIgnore
  public RepositoryDefinition getRepositoryDefinition() {
    return repositoryDefinition;
  }

  public String getRepositoryName() {
    return repositoryDefinition == null ? null : repositoryDefinition.getName();
  }

  public String getName() {
    return name;
  }

  public void setRepositoryDefinition(RepositoryDefinition repositoryDefinition) {
    this.repositoryDefinition = repositoryDefinition;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public void setLibraryRoot(String libraryRoot) {
    this.libraryRoot = libraryRoot;
  }

  public void setRefreshInterval(long refreshInterval) {
    this.refreshInterval = refreshInterval;
  }

  public String getBranch() {
    return branch;
  }

  public String getLibraryRoot() {
    return libraryRoot;
  }

  public long getRefreshInterval() {
    return refreshInterval;
  }

  public void setImmutable(boolean immutable) {
    this.immutable = immutable;
  }

  public boolean isImmutable() {
    return immutable;
  }

  @Override
  public ContextDefinition clone() {
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
    result = prime * result + ((libraryRoot == null) ? 0 : libraryRoot.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + (int) (refreshInterval ^ (refreshInterval >>> 32));
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
    if (libraryRoot == null) {
      if (other.libraryRoot != null)
        return false;
    } else if (!libraryRoot.equals(other.libraryRoot))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (refreshInterval != other.refreshInterval)
      return false;
    if (repositoryDefinition == null) {
      if (other.repositoryDefinition != null)
        return false;
    } else if (!repositoryDefinition.equals(other.repositoryDefinition))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return getBranch() + "@" + getName();
  }

  @Override
  public int compareTo(ContextDefinition o) {
    return getName().compareTo(o.getName());
  }
}
