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

import java.util.List;
import java.util.Map;

import net.riezebos.thoth.exceptions.ContextManagerException;

public interface ContextManager {

  public List<String> getContexts() throws ContextManagerException;

  public Map<String, ContextDefinition> getContextDefinitions() throws ContextManagerException;

  public ContextDefinition getContextDefinition(String contextName) throws ContextManagerException;

  public ContextDefinition createContextDefinition(ContextDefinition contextDefinition) throws ContextManagerException;

  public boolean updateContextDefinition(ContextDefinition contextDefinition) throws ContextManagerException;

  public void deleteContextDefinition(ContextDefinition contextDefinition) throws ContextManagerException;

  // RepositoryDefinition related

  public Map<String, RepositoryDefinition> getRepositoryDefinitions() throws ContextManagerException;

  public RepositoryDefinition getRepositoryDefinition(String repositoryName) throws ContextManagerException;

  public RepositoryDefinition createRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException;

  public boolean updateRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException;

  public boolean deleteRepositoryDefinition(RepositoryDefinition repositoryDefinition) throws ContextManagerException;

  boolean isInUse(RepositoryDefinition repositoryDefinition) throws ContextManagerException;

  public boolean isValidContext(String context) throws ContextManagerException;

}
