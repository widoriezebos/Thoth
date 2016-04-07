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
    Thread.sleep(500);
    assertNull(cache.get(KEY));
  }

}
