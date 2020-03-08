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
package net.riezebos.thoth.util;

import java.io.IOException;
import java.util.Properties;

public class MimeTypeUtil {
  private static final String MAPFILE = "net/riezebos/thoth/util/mimetypes.properties";
  private static Properties types = null;

  public static String getMimeType(String extension) {
    if (extension == null)
      return null;

    if (types == null) {
      try {
        Properties props = new Properties();
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(MAPFILE));
        types = props;
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
    return (String) types.get(extension.toLowerCase());
  }
}
