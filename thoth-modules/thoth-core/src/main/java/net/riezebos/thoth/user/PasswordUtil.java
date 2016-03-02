package net.riezebos.thoth.user;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordUtil {
  private static final Logger LOG = LoggerFactory.getLogger(User.class);

  public String encodePassword(String clearTextPassword) {
    if (clearTextPassword == null || clearTextPassword.trim().length() == 0)
      return "";
    StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    return passwordEncryptor.encryptPassword(clearTextPassword);
  }

  public boolean isValidPassword(String clearTextPassword, String passwordhash) {
    if (!StringUtils.isBlank(clearTextPassword) && StringUtils.isBlank(passwordhash))
      return false;
    if (StringUtils.isBlank(clearTextPassword) && !StringUtils.isBlank(passwordhash))
      return false;
    if (StringUtils.isBlank(clearTextPassword) && StringUtils.isBlank(passwordhash))
      return true;

    StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    try {
      return passwordEncryptor.checkPassword(clearTextPassword, passwordhash);
    } catch (Exception x) {
      LOG.error(x.getMessage(), x);
      return false;
    }
  }

}
