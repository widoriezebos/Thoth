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

public class ContextDefinition {

  private RepositoryDefinition repositoryDefinition;
  private String name;
  private String branch;
  private long refreshIntervalMS;

  public ContextDefinition(RepositoryDefinition repositoryDefinition, String name, String branch, long refreshIntervalMS) {
    super();
    this.repositoryDefinition = repositoryDefinition;
    this.name = name;
    this.branch = branch;
    this.refreshIntervalMS = refreshIntervalMS;
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

  public long getRefreshIntervalMS() {
    return refreshIntervalMS;
  }
}
