package net.riezebos.thoth.configuration;

public interface ConfigurationChangeListener {

  public void contextAdded(ContextDefinition context);

  public void contextRemoved(ContextDefinition context);

  public void renderersChanged();

}
