package net.riezebos.thoth.user.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BaseDao {
  private List<CacheListener> listeners = new ArrayList<>();

  public void registerCacheListener(CacheListener listener) {
    listeners.add(listener);
  }

  protected void reloadCaches() {
    for (CacheListener listener : listeners)
      listener.invalidateCache();
  }
  
  protected void commitReload(Connection connection) throws SQLException {
    connection.commit();
    reloadCaches();
  }

  protected void commitOnly(Connection connection) throws SQLException {
    connection.commit();
  }

}
