package net.riezebos.thoth.configuration;

import net.riezebos.thoth.context.ContextDefinition;

public interface ConfigurationChangeListener {

  public void contextAdded(ContextDefinition context);

  public void contextRemoved(ContextDefinition context);

  public void renderersChanged();

}
