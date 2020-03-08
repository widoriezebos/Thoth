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
package net.riezebos.thoth.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExpiringCacheTest {

  private static final String VALUE = "value";
  private static final String KEY = "key";
  private static final String KEY2 = "key2";
  private static final String VALUE2 = "value2";

  @Test
  public void test() throws InterruptedException {

    ExpiringCache<String, String> cache = new ExpiringCache<>(500);

    cache.put(KEY, VALUE);

    assertEquals(VALUE, cache.get(KEY));
    assertTrue(cache.getKeys().contains(KEY));
    assertTrue(cache.getValues().contains(VALUE));

    cache.put(KEY2, VALUE2);
    assertTrue(cache.getKeys().contains(KEY2));
    cache.remove(KEY2);
    assertFalse(cache.getKeys().contains(KEY2));
    Thread.sleep(510);
    assertNull(cache.get(KEY));
  }

}
