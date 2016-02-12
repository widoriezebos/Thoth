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
package net.riezebos.thoth.markdown.filehandle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface FileHandle {

  String getName();

  boolean exists();

  boolean isFile();

  boolean isDirectory();

  long lastModified();

  String getCanonicalPath() throws IOException;

  String getAbsolutePath();

  String[] list();

  FileHandle getParentFile();

  InputStream getInputStream() throws FileNotFoundException;

}
