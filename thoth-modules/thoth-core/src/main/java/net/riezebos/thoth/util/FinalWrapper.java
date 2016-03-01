package net.riezebos.thoth.util;

public class FinalWrapper<T> {
  public final T value;

  public FinalWrapper(T value) {
    this.value = value;
  }
}
