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
package net.riezebos.thoth.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoRefresher extends Thread {
  private static final int MIN_INTERVAL = 1000;
  private static final Logger LOG = LoggerFactory.getLogger(AutoRefresher.class);
  private static int SLICE = 100;

  private long interval;
  private ContentManager contentManager;
  private boolean cancelRequested = false;

  public AutoRefresher(long interval, ContentManager contentManager) {

    if (interval < MIN_INTERVAL)
      interval = 1000;
    this.interval = interval;
    this.contentManager = contentManager;

    start();
  }

  public void cancel() {
    cancelRequested = true;
  }

  @Override
  public void run() {
    LOG.debug("AutoRefresher started it's work");
    while (!cancelRequested) {
      try {
        for (int i = 0; i < SLICE && !cancelRequested; i++) {
          sleep(interval / SLICE);
        }
        if (!cancelRequested)
          contentManager.refresh();
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }
    LOG.debug("AutoRefresher done");
  }

}
