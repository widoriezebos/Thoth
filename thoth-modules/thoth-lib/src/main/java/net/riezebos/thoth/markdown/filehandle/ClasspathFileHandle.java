package net.riezebos.thoth.markdown.filehandle;

import java.io.FileNotFoundException;
import java.io.InputStream;

import net.riezebos.thoth.util.ThothUtil;

public class ClasspathFileHandle implements FileHandle {

  private static final long serialVersionUID = 1L;
  ClasspathFileHandleFactory fileHandleFactory;
  private String fullPath;

  public ClasspathFileHandle(ClasspathFileHandleFactory fileHandleFactory, String fullPath) {
    this.fileHandleFactory = fileHandleFactory;
    this.fullPath = fullPath;
  }

  @Override
  public String getName() {
    return ThothUtil.getFileName(fullPath);
  }

  @Override
  public boolean exists() {
    return fileHandleFactory.exists(fullPath);
  }

  @Override
  public boolean isFile() {
    return fileHandleFactory.isFile(fullPath);
  }

  @Override
  public boolean isDirectory() {
    return fileHandleFactory.isDirectory(fullPath);
  }

  @Override
  public long lastModified() {
    return fileHandleFactory.lastModified(fullPath);
  }

  @Override
  public String getCanonicalPath() {
    return ThothUtil.getCanonicalPath(fullPath);
  }

  @Override
  public String getAbsolutePath() {
    return fullPath;
  }

  @Override
  public String[] list() {
    return fileHandleFactory.list(getCanonicalPath());
  }

  @Override
  public FileHandle[] listFiles() {
    String[] strings = list();
    FileHandle[] result = new FileHandle[strings.length];
    for (int i = 0; i < strings.length; i++)
      result[i] = new ClasspathFileHandle(fileHandleFactory, strings[i]);
    return result;
  }

  @Override
  public FileHandle getParentFile() {
    String parentName = ThothUtil.getPartBeforeLast(fullPath, "/");
    return fileHandleFactory.createFileHandle(parentName);
  }

  @Override
  public InputStream getInputStream() throws FileNotFoundException {
    String resourcePath = ThothUtil.stripPrefix(fullPath, "/");
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
    if (is == null)
      throw new FileNotFoundException("classpath:" + resourcePath);
    return is;
  }

  @Override
  public String toString() {
    return getAbsolutePath();
  }

  @Override
  public int compareTo(FileHandle other) {
    if (other instanceof ClasspathFileHandle)
      return fullPath.compareTo(((ClasspathFileHandle) other).fullPath);
    else
      return -1;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    ClasspathFileHandle other = (ClasspathFileHandle) obj;
    if (fullPath == null) {
      if (other.fullPath != null)
        return false;
    } else if (!fullPath.equals(other.fullPath))
      return false;
    return true;
  }

}
