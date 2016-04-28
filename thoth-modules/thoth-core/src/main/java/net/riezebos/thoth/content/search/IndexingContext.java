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
package net.riezebos.thoth.content.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.riezebos.thoth.markdown.util.ProcessorError;

public class IndexingContext {
  private Map<String, List<String>> indirectReverseIndex = new HashMap<>();
  private Map<String, List<String>> directReverseIndex = new HashMap<>();
  private Set<ProcessorError> errors = new HashSet<>();
  private Set<String> referencedLocalResources = new HashSet<>();
  private Set<String> allPaths = new HashSet<>();

  public Map<String, List<String>> getIndirectReverseIndex() {
    return indirectReverseIndex;
  }

  public Map<String, List<String>> getDirectReverseIndex() {
    return directReverseIndex;
  }

  public Set<ProcessorError> getErrors() {
    return errors;
  }

  public Set<String> getReferencedLocalResources() {
    return referencedLocalResources;
  }

  public Set<String> getAllPaths() {
    return allPaths;
  }
}
