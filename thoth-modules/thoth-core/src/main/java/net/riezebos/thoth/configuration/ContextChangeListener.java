package net.riezebos.thoth.configuration;

public interface ContextChangeListener {

  public void contextAdded(ContextDefinition context);

  public void contextRemoved(ContextDefinition context);

}
