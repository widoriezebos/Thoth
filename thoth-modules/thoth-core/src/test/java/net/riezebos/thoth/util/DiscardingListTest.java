/* Copyright (c) 2020 W.T.J. Riezebos
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
package net.riezebos.thoth.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class DiscardingListTest {

  @Test
  public void test() {
    DiscardingList<String> lst = new DiscardingList<>();
    String nope = "nope";
    List<String> oneTwo = Arrays.asList(new String[] {"one", "two"});
    lst.add(nope);
    lst.add(0, nope);
    lst.addAll(oneTwo);
    lst.addAll(0, oneTwo);

    assertEquals(0, lst.size());
    assertTrue(lst.isEmpty());
    assertFalse(lst.contains(nope));
    assertFalse(lst.iterator().hasNext());
    assertEquals(0, lst.toArray().length);
    assertEquals(0, lst.toArray(new String[0]).length);
    assertFalse(lst.remove(nope));
    assertNull(lst.remove(0));
    assertFalse(lst.containsAll(oneTwo));
    assertFalse(lst.removeAll(oneTwo));
    assertFalse(lst.retainAll(oneTwo));

    lst.clear();
    assertEquals(0, lst.size());
    assertNull(lst.get(0));
    assertNull(lst.set(0, "no"));
    assertEquals(-1, lst.indexOf("nope"));
    assertEquals(-1, lst.lastIndexOf("nope"));
    assertFalse(lst.listIterator().hasNext());
    assertFalse(lst.listIterator(0).hasNext());
    assertTrue(lst.subList(0, 10).isEmpty());
  }

}
