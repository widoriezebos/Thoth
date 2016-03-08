package net.riezebos.thoth.content.impl.util;

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.impl.FSContentManager;
import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.exceptions.ContentManagerException;

public class TestFSContentManager extends FSContentManager {

  public TestFSContentManager(ContextDefinition contextDefinition, ThothEnvironment context) throws ContentManagerException {
    super(contextDefinition, context);
  }

  @Override
  protected void notifyContextContentsChanged() {
    // Do not fire an indexer
  }

}
