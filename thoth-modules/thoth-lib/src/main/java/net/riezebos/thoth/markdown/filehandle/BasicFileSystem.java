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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import net.riezebos.thoth.util.ThothUtil;

public class BasicFileSystem implements FileSystem {

  private String fileSystemRoot;
  private Set<String> createdFiles = new HashSet<String>();

  public BasicFileSystem() {
    this(null);
  }

  public BasicFileSystem(String fileSystemRoot) {
    fileSystemRoot = ThothUtil.normalSlashes(fileSystemRoot);
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
    if (fileHandle == null)
      return null;
    String localPath = ThothUtil.prefix(fileHandle.getCanonicalPath(), "/");
    if (denied(localPath))
      throw new IllegalArgumentException("Access denied");
    String fileName = fileSystemRoot + localPath;
    return new File(fileName);
  }

  @Override
  public boolean exists(FileHandle fileHandle) {
    return fileHandle == null ? false : getFile(fileHandle).exists();
  }

  @Override
  public boolean delete(FileHandle fileHandle) {
    return fileHandle == null ? false : getFile(fileHandle).delete();
  }

  @Override
  public boolean isFile(FileHandle fileHandle) {
    return fileHandle == null ? false : getFile(fileHandle).isFile();
  }

  @Override
  public boolean isDirectory(FileHandle fileHandle) {
    return fileHandle == null ? false : getFile(fileHandle).isDirectory();
  }

  @Override
  public long lastModified(FileHandle fileHandle) {
    return fileHandle == null ? 0L : getFile(fileHandle).lastModified();
  }

  @Override
  public long length(FileHandle fileHandle) {
    return fileHandle == null ? 0L : getFile(fileHandle).length();
  }

  @Override
  public String[] list(FileHandle fileHandle) {
    return fileHandle == null ? null : getFile(fileHandle).list();
  }

  @Override
  public FileHandle[] listFiles(FileHandle fileHandle) {
    if (fileHandle == null)
      return null;
    File[] listFiles = getFile(fileHandle).listFiles();
    if (listFiles == null)
      return null;
    FileHandle[] result = new FileHandle[listFiles.length];
    for (int i = 0; i < listFiles.length; i++) {
      String canonicalPath = ThothUtil.getCanonicalPath(ThothUtil.normalSlashes(listFiles[i].getAbsolutePath()));
      if (!canonicalPath.startsWith(fileSystemRoot))
        throw new IllegalArgumentException(fileHandle + " is not located under " + fileSystemRoot);

      result[i] = new FileHandle(this, canonicalPath.substring(fileSystemRoot.length()));
    }
    return result;
  }

  protected boolean denied(String path) {
    return (path.startsWith("../") || path.startsWith("/../"));
  }

  @Override
  public InputStream getInputStream(FileHandle fileHandle) throws FileNotFoundException {
    if (denied(fileHandle.getCanonicalPath()))
      return null;

    try {
      return new FileInputStream(getFile(fileHandle));
    } catch (FileNotFoundException e) {
      throw new FileNotFoundException(fileHandle.getAbsolutePath());
    }
  }

  @Override
  public OutputStream getOutputStream(FileHandle fileHandle) throws IOException {
    String canonicalPath = fileHandle.getCanonicalPath();

    // Implicitly create any paths we need
    File file = getFile(fileHandle);
    File parent = file.getParentFile();
    parent.mkdirs();
    createdFiles.add(canonicalPath);
    return new FileOutputStream(file);
  }

  @Override
  public Set<String> getCreatedFiles() {
    return createdFiles;
  }
}
