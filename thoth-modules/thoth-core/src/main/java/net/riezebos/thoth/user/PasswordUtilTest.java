package net.riezebos.thoth.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordUtilTest {

  @Test
  public void test() {
    PasswordUtil util = new PasswordUtil();
    String clearTextPassword = "Welcome2Thoth!";
    String encodedPassword = util.encodePassword(clearTextPassword);
    String encodedPassword2 = util.encodePassword(clearTextPassword);
    assertNotEquals(encodedPassword, encodedPassword2);
    assertTrue(util.isValidPassword(clearTextPassword, encodedPassword));
    assertTrue(util.isValidPassword(clearTextPassword, encodedPassword2));
    assertFalse(util.isValidPassword(clearTextPassword + "nope", encodedPassword2));
  }

}
