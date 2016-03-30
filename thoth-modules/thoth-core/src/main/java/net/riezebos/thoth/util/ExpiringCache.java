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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExpiringCache<K, T> implements Serializable {
  private static final long serialVersionUID = 1L;
  private Map<K, T> cache = new HashMap<K, T>();
  private Map<K, Long> maximumAge = new HashMap<K, Long>();
  private long maximumAgeMillis;

  public ExpiringCache(long maxAgeMillis) {
    maximumAgeMillis = maxAgeMillis;
  }

  public void put(K key, T value) {
    synchronized (this) {
      cache.put(key, value);
      maximumAge.put(key, System.currentTimeMillis() + maximumAgeMillis);
    }
  }

  public T get(K key) {
    purgeExpired();
    synchronized (this) {
      T result = cache.get(key);
      if (result != null) {
        long maxAge = maximumAge.get(key);
        if (maxAge < System.currentTimeMillis()) {
          result = null;
          doRemove(key);
        }
      }
      return result;
    }
  }

  public void remove(K key) {
    synchronized (this) {
      doRemove(key);
    }
  }

  public void purgeExpired() {
    synchronized (this) {
      Set<K> toBeRemoved = new HashSet<K>();

      for (Map.Entry<K, Long> entry : maximumAge.entrySet())
        if (entry.getValue() < System.currentTimeMillis())
          toBeRemoved.add(entry.getKey());

      for (K key : toBeRemoved)
        doRemove(key);
    }
  }

  protected void doRemove(K key) {
    cache.remove(key);
    maximumAge.remove(key);
  }

  public Collection<T> getValues() {
    purgeExpired();
    synchronized (this) {
      return new ArrayList<T>(cache.values());
    }
  }

  public Set<K> getKeys() {
    purgeExpired();
    synchronized (this) {
      return new HashSet<K>(cache.keySet());
    }
  }

}
