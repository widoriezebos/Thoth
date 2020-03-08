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
package net.riezebos.thoth.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class AccessRuleTest {

  @Test
  public void test() {

    AccessRule accessRule = new AccessRule("path/*", "group1, group2; group3", true, false);

    assertTrue(accessRule.applies("path/something"));
    assertTrue(accessRule.applies("path/something/else"));
    assertTrue(accessRule.applies("/path/something"));
    assertFalse(accessRule.applies("/nope/something"));

    Set<String> allGroups = new HashSet<>(Arrays.asList(new String[] {"group1", "group2", "group3"}));
    Set<String> just12 = new HashSet<>(Arrays.asList(new String[] {"group1", "group2"}));
    Set<String> just1 = new HashSet<>(Arrays.asList(new String[] {"group1"}));
    Set<String> other = new HashSet<>(Arrays.asList(new String[] {"other"}));

    assertTrue(accessRule.isAccessAllowed(allGroups));
    assertFalse(accessRule.isAccessAllowed(just12));
    assertFalse(accessRule.isAccessAllowed(just1));
    assertFalse(accessRule.isAccessAllowed(other));

    accessRule = new AccessRule("path/*", "group1, group2; group3", false, false);
    assertTrue(accessRule.isAccessAllowed(allGroups));
    assertTrue(accessRule.isAccessAllowed(just12));
    assertTrue(accessRule.isAccessAllowed(just1));
    assertFalse(accessRule.isAccessAllowed(other));
  }

}
