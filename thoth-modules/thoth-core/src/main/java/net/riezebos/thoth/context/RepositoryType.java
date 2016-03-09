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

public enum RepositoryType {

  GIT("git"), FILESYSTEM("filesystem", "fs"), CLASSPATH("classpath", "cp"), NOP("nop"), ZIP("zip");

  private String value;
  private String[] aliases;

  private RepositoryType(String... aliases) {
    this.value = aliases[0];
    this.aliases = aliases;
  }

  public String getValue() {
    return value;
  }

  public static RepositoryType convert(String value) {

    for (RepositoryType type : RepositoryType.values())
      for (String alias : type.aliases)
        if (alias.equalsIgnoreCase(value))
          return type;
    throw new IllegalArgumentException("Cannot convert " + value);
  }

  @Override
  public String toString() {
    return value;
  }
}