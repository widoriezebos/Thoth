package net.riezebos.thoth.content.impl.util;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.content.impl.FSContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class TestFSContentManager extends FSContentManager {

  public TestFSContentManager(ContextDefinition contextDefinition, Configuration configuration) throws ContentManagerException {
    super(contextDefinition, configuration);
  }

  @Override
  protected void notifyContextContentsChanged() {
    // Do not fire an indexer
  }

}
