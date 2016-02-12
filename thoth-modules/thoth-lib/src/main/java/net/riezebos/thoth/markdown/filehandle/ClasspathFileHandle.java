package net.riezebos.thoth.markdown.filehandle;

import java.io.FileNotFoundException;
import java.io.InputStream;

import net.riezebos.thoth.util.ThothUtil;

public class ClasspathFileHandle implements FileHandle {

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
}
