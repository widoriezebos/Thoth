/* Copyright (c) 2020 W.T.J. Riezebos
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

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordUtil {
  private static final Logger LOG = LoggerFactory.getLogger(User.class);

  public String hashPassword(String clearTextPassword) {
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

  public String encrypt(String masterPassword, String clearTextPassword) {
    PBEStringEncryptor encrypter = getEncryptor(masterPassword);
    return encrypter.encrypt(clearTextPassword);
  }

  public String decrypt(String masterPassword, String encryptedPassword) {
    PBEStringEncryptor encrypter = getEncryptor(masterPassword);
    return encrypter.decrypt(encryptedPassword);
  }

  protected PBEStringEncryptor getEncryptor(String masterPassword) {
    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    encryptor.setPassword(masterPassword);
    SimplePBEConfig config = new SimplePBEConfig();
    config.setAlgorithm("PBEWithMD5AndDES");
    config.setKeyObtentionIterations(StandardPBEByteEncryptor.DEFAULT_KEY_OBTENTION_ITERATIONS);
    config.setPassword(masterPassword);
    encryptor.setConfig(config);
    return encryptor;
  }

}
