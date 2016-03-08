/* Copyright (c) 2016 W.T.J. Riezebos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.riezebos.thoth.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordUtilTest {

  @Test
  public void test() {
    PasswordUtil util = new PasswordUtil();
    String clearTextPassword = "SomeTestPW!";
    String encodedPassword = util.hashPassword(clearTextPassword);
    String encodedPassword2 = util.hashPassword(clearTextPassword);
    assertNotEquals(encodedPassword, encodedPassword2);
    assertTrue(util.isValidPassword(clearTextPassword, encodedPassword));
    assertTrue(util.isValidPassword(clearTextPassword, encodedPassword2));
    assertFalse(util.isValidPassword(clearTextPassword + "nope", encodedPassword2));
    
    String masterPassword = "secret";
    String encrypted = util.encrypt(masterPassword, clearTextPassword);
    
    assertEquals(clearTextPassword, util.decrypt(masterPassword, encrypted));
  }

}
