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

import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.DatabaseException;
import net.riezebos.thoth.exceptions.UserManagerException;
import net.riezebos.thoth.user.dao.IdentityDao;

/**
 * @author wido
 */
public class BasicUserManager implements UserManager {

  IdentityDao identityDao;

  public BasicUserManager(ThothEnvironment thothEnvironment) throws UserManagerException {
    try {
      identityDao = new IdentityDao(thothEnvironment.getThothDB());
    } catch (DatabaseException e) {
      throw new UserManagerException(e);
    }
  }

  @Override
  public User getUser(String identifier) throws UserManagerException {
    Identity identity = identityDao.getIdentities().get(identifier);
    if (identity instanceof User)
      return (User) identity;
    return null;
  }

  @Override
  public Group getGroup(String identifier) throws UserManagerException {
    Identity identity = identityDao.getIdentities().get(identifier);
    if (identity instanceof Group)
      return (Group) identity;
    return null;
  }
}
