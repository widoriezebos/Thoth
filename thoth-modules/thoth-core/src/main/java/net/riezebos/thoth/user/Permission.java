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
package net.riezebos.thoth.user;

public enum Permission {

  ACCESS(1),
  READ_BOOKS(2),
  READ_FRAGMENTS(3),
  READ_RESOURCE(4),
  BROWSE(5),
  DIFF(6),
  META(7),
  PULL(8),
  REINDEX(9),
  REVISION(10),
  SEARCH(11),
  VALIDATE(12),
  MANAGE_USERS(13),
  MANAGE_CONTEXTS(14);

  private int value;

  private Permission(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static Permission convert(int intValue) {

    for (Permission permission : Permission.values())
      if (permission.value == intValue)
        return permission;
    throw new IllegalArgumentException("Cannot convert " + intValue);
  }
}
