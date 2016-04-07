package net.riezebos.thoth.exceptions;

public class UserManagerException extends ContentManagerException {
  private static final long serialVersionUID = 1L;

  public UserManagerException(Exception e) {
    super(e);
  }

  public UserManagerException(String message) {
    super(message);
  }

}
