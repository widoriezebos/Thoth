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
import java.io.OutputStream;
import java.io.Serializable;

import net.riezebos.thoth.util.ThothUtil;

public class FileHandle implements Serializable, Comparable<FileHandle> {

  private static final long serialVersionUID = 1L;
  private FileSystem fileSystem;
  private String fullPath;

  public FileHandle(FileSystem fileSystem, String fullPath) {
    this.fileSystem = fileSystem;
    this.fullPath = fullPath;
  }

  public FileSystem getFileSystem() {
    return fileSystem;
  }

  public String getName() {
    return ThothUtil.getFileName(fullPath);
  }

  public String getCanonicalPath() {
    return ThothUtil.getCanonicalPath(fullPath);
  }

  public String getAbsolutePath() {
    return fullPath;
  }

  public boolean delete() {
    return fileSystem.delete(this);
  }

  public boolean exists() {
    return fileSystem.exists(this);
  }

  public boolean isFile() {
    return fileSystem.isFile(this);
  }

  public boolean isDirectory() {
    return fileSystem.isDirectory(this);
  }

  public long lastModified() {
    return fileSystem.lastModified(this);
  }

  public long length() {
    return fileSystem.length(this);
  }

  public String[] list() {
    return fileSystem.list(this);
  }

  public FileHandle[] listFiles() {
    return fileSystem.listFiles(this);
  }

  public FileHandle getParentFile() {
    if (fullPath == null || fullPath.length() == 0 || fullPath.indexOf("/") == -1 || fullPath.equals("/"))
      return null;
    String parentName = ThothUtil.getPartBeforeLast(fullPath, "/");
    return fileSystem.getFileHandle(parentName);
  }

  public InputStream getInputStream() throws IOException {
    return fileSystem.getInputStream(this);
  }

  public OutputStream getOutputStream() throws IOException {
    return fileSystem.getOutputStream(this);
  }

  @Override
  public String toString() {
    return getAbsolutePath();
  }

  @Override
  public int compareTo(FileHandle other) {
    if (other instanceof FileHandle)
      return fullPath.compareTo(((FileHandle) other).fullPath);
    else
      return -1;
  }

  public void importTree(FileHandle rootSource) throws IOException {
    if (!this.isDirectory())
      throw new IOException("Can only import to a directory; " + this.getAbsolutePath() + " is file");
    importTree(rootSource, rootSource, this);
  }

  protected void importTree(FileHandle originalRoot, FileHandle rootSource, FileHandle targetFolder) throws IOException {

    if (!targetFolder.isDirectory())
      throw new IOException("Destination " + targetFolder.getAbsolutePath() + " is not a directory");
    if (rootSource.isDirectory()) {
      for (FileHandle source : rootSource.listFiles()) {
        if (source.isDirectory())
          importTree(originalRoot, source, targetFolder);
        else {
          String originalCanonicalPath = originalRoot.getCanonicalPath();
          String currentCanonicalPath = rootSource.getCanonicalPath();
          String currentPrefix = ThothUtil.suffix(currentCanonicalPath.substring(originalCanonicalPath.length()), "/");

          String destPath = ThothUtil.suffix(targetFolder.getAbsolutePath(), "/") + currentPrefix + source.getName();
          // Special case for root copies
          destPath = destPath.replaceAll("//", "/");
          FileHandle destFile = targetFolder.getFileSystem().getFileHandle(destPath);
          OutputStream outputStream = null;
          InputStream inputStream = null;
          try {
            outputStream = destFile.getOutputStream();
            inputStream = source.getInputStream();
            FileHandleUtil.copy(inputStream, outputStream);
          } finally {
            if (outputStream != null)
              outputStream.close();
            if (inputStream != null)
              inputStream.close();
          }
        }
      }
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fileSystem == null) ? 0 : fileSystem.hashCode());
    result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FileHandle other = (FileHandle) obj;
    if (fileSystem == null) {
      if (other.fileSystem != null)
        return false;
    } else if (fileSystem != other.fileSystem)
      return false;
    if (fullPath == null) {
      if (other.fullPath != null)
        return false;
    } else if (!fullPath.equals(other.fullPath))
      return false;
    return true;
  }
}
