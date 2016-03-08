package net.riezebos.thoth.exceptions;

public class ContextManagerException extends ContentManagerException {
  private static final long serialVersionUID = 1L;

  public ContextManagerException(Exception e) {
    super(e);
  }

  public ContextManagerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ContextManagerException(String message) {
    super(message);
  }

}
