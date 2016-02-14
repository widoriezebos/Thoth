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

import java.io.IOException;
import java.io.InputStream;

public interface FileSystem {

  FileHandle getFileHandle(String filename);

  boolean exists(FileHandle fileHandle);

  boolean isFile(FileHandle fileHandle);

  boolean isDirectory(FileHandle fileHandle);

  long lastModified(FileHandle fileHandle);

  long length(FileHandle fileHandle);

  String[] list(FileHandle fileHandle);

  FileHandle[] listFiles(FileHandle fileHandle);

  InputStream getInputStream(FileHandle fileHandle) throws IOException;
}
