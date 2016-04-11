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
 */package net.riezebos.thoth.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.exceptions.ConfigurationException;

public class HotReloadableConfigurationTest implements ConfigurationChangeListener {

  private List<ContextDefinition> added;
  private List<ContextDefinition> removed;
  private boolean renderersChanged;

  @Test
  public void test() throws ConfigurationException, FileNotFoundException {
    String propFile = "net/riezebos/thoth/configuration/test.configuration.properties";
    String propFile2 = "net/riezebos/thoth/configuration/test.configuration2.properties";
    PropertyBasedConfiguration config = new PropertyBasedConfiguration();
    config.setPropertyFileName("classpath:" + propFile);
    config.reload();
    config.validate();

    HotReloadableConfiguration hotConfig = new HotReloadableConfiguration(config);

    hotConfig.addConfigurationChangeListener(this);
    renderersChanged = false;
    added = new ArrayList<>();
    removed = new ArrayList<>();
    hotConfig.reload();
    assertFalse(renderersChanged);
    assertTrue(added.isEmpty());
    assertTrue(removed.isEmpty());

    PropertyBasedConfiguration activeConfiguration = (PropertyBasedConfiguration) hotConfig.getActiveConfiguration();
    activeConfiguration.setPropertyFileName("classpath:" + propFile2);
    hotConfig.reload();
    assertEquals(1, added.size());
    assertEquals(1, removed.size());
    assertTrue(renderersChanged);
    hotConfig.discard();
  }

  @Test
  public void testAutoReload() throws ConfigurationException, FileNotFoundException, InterruptedException {
    String propFile = "net/riezebos/thoth/configuration/test.configuration3.properties";
    String propFile2 = "net/riezebos/thoth/configuration/test.configuration2.properties";
    PropertyBasedConfiguration config = new PropertyBasedConfiguration();
    config.setPropertyFileName("classpath:" + propFile);
    config.reload();
    config.validate();

    HotReloadableConfiguration hotConfig = new HotReloadableConfiguration(config) {
      @Override
      protected long getModificationTime(Configuration configuration) {
        return System.currentTimeMillis();
      }
    };
    renderersChanged = false;
    
    added = new ArrayList<>();
    removed = new ArrayList<>();

    hotConfig.addConfigurationChangeListener(this);
    PropertyBasedConfiguration activeConfiguration = (PropertyBasedConfiguration) hotConfig.getActiveConfiguration();
    activeConfiguration.setPropertyFileName("classpath:" + propFile2);

    Thread.sleep(1500L);

    assertEquals(1, added.size());
    assertEquals(1, removed.size());
    assertTrue(renderersChanged);
    hotConfig.discard();
  }

  @Override
  public void contextAdded(ContextDefinition context) {
    added.add(context);
  }

  @Override
  public void contextRemoved(ContextDefinition context) {
    removed.add(context);
  }

  @Override
  public void renderersChanged() {
    renderersChanged = true;
  }

}
