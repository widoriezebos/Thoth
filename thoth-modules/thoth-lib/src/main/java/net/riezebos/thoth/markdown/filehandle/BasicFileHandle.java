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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.riezebos.thoth.util.ThothUtil;

/**
 * Very basic FileHandle around a normal file
 * 
 * @author wido
 */
public class BasicFileHandle implements FileHandle {
  private static final long serialVersionUID = 1L;
  private File file;

  public BasicFileHandle(String fileName) {
    file = new File(fileName);
  }

  public BasicFileHandle(File file) {
    this.file = file;
  }

  @Override
  public boolean exists() {
    return file.exists();
  }

  @Override
  public String getCanonicalPath() throws IOException {
    return ThothUtil.getCanonicalPath(file);
  }

  @Override
  public String getAbsolutePath() {
    return file.getAbsolutePath();
  }

  @Override
  public boolean isFile() {
    return file.isFile();
  }

  @Override
  public long lastModified() {
    return file.lastModified();
  }

  @Override
  public InputStream getInputStream() throws FileNotFoundException {
    return new FileInputStream(file);
  }

  @Override
  public boolean isDirectory() {
    return file.isDirectory();
  }

  @Override
  public String[] list() {
    return file.list();
  }

  @Override
  public FileHandle[] listFiles() {
    File[] files = file.listFiles();
    if (files == null)
      return null;

    List<FileHandle> result = new ArrayList<FileHandle>();
    for (File file : files)
      result.add(new BasicFileHandle(file));

    return result.toArray(new FileHandle[result.size()]);
  }

  @Override
  public FileHandle getParentFile() {
    String parent = file.getParent();
    if (parent == null)
      return null;
    return new BasicFileHandle(parent);
  }

  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  public String toString() {
    return getAbsolutePath();
  }

  @Override
  public int compareTo(FileHandle other) {
    if (other instanceof BasicFileHandle)
      return file.compareTo(((BasicFileHandle) other).file);
    else
      return -1;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((file == null) ? 0 : file.hashCode());
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
    BasicFileHandle other = (BasicFileHandle) obj;
    if (file == null) {
      if (other.file != null)
        return false;
    } else if (!file.equals(other.file))
      return false;
    return true;
  }
}
