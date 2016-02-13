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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import net.riezebos.thoth.util.ThothUtil;

public class BasicFileSystem implements FileSystem {

  private String fileSystemRoot;

  public BasicFileSystem() {
    this(null);
  }

  public BasicFileSystem(String fileSystemRoot) {
    if (fileSystemRoot == null)
      fileSystemRoot = "/";
    if (!"/".equals(fileSystemRoot))
      fileSystemRoot = ThothUtil.stripSuffix(fileSystemRoot, "/");
    this.fileSystemRoot = ThothUtil.getCanonicalPath(fileSystemRoot);
  }

  @Override
  public FileHandle getFileHandle(String fileName) {
    String path = ThothUtil.getCanonicalPath(ThothUtil.stripSuffix(ThothUtil.prefix(fileName, "/"), "/"));
    return new FileHandle(this, path);
  }

  protected File getFile(FileHandle fileHandle) {
    String fileName = fileSystemRoot + ThothUtil.prefix(fileHandle.getCanonicalPath(), "/");
    return new File(fileName);
  }

  @Override
  public boolean exists(FileHandle fileHandle) {
    return getFile(fileHandle).exists();
  }

  @Override
  public boolean isFile(FileHandle fileHandle) {
    return getFile(fileHandle).isFile();
  }

  @Override
  public boolean isDirectory(FileHandle fileHandle) {
    return getFile(fileHandle).isDirectory();
  }

  @Override
  public long lastModified(FileHandle fileHandle) {
    return getFile(fileHandle).lastModified();
  }

  @Override
  public long length(FileHandle fileHandle) {
    return getFile(fileHandle).length();
  }

  @Override
  public String[] list(FileHandle fileHandle) {
    return getFile(fileHandle).list();
  }

  @Override
  public FileHandle[] listFiles(FileHandle fileHandle) {
    File[] listFiles = getFile(fileHandle).listFiles();
    if (listFiles == null)
      return null;
    FileHandle[] result = new FileHandle[listFiles.length];
    for (int i = 0; i < listFiles.length; i++) {
      String canonicalPath = ThothUtil.getCanonicalPath(listFiles[i]);
      if (!canonicalPath.startsWith(fileSystemRoot))
        throw new IllegalArgumentException(fileHandle + " is not located under " + fileSystemRoot);

      result[i] = new FileHandle(this, canonicalPath.substring(fileSystemRoot.length()));
    }
    return result;
  }

  @Override
  public InputStream getInputStream(FileHandle fileHandle) throws FileNotFoundException {
    return new FileInputStream(getFile(fileHandle));
  }

}
