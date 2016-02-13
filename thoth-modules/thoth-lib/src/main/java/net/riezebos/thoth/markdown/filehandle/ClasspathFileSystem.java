package net.riezebos.thoth.markdown.filehandle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.util.ThothUtil;

public class ClasspathFileSystem implements FileSystem {

  private String fileSystemRoot;
  private List<String> folders = new ArrayList<String>();
  private Map<String, List<String>> folderContents = new HashMap<String, List<String>>();
  private Map<String, Long> modified = new HashMap<String, Long>();
  private Map<String, Long> lengths = new HashMap<String, Long>();

  public ClasspathFileSystem() {
    this(null);
  }

  public ClasspathFileSystem(String fileSystemRoot) {
    if (fileSystemRoot == null)
      fileSystemRoot = "";
    if (!"".equals(fileSystemRoot))
      fileSystemRoot = ThothUtil.stripSuffix(fileSystemRoot, "/");
    this.fileSystemRoot = fileSystemRoot;
  }

  public void registerFile(String spec) {
    String folder = ThothUtil.prefix(ThothUtil.getFolder(spec), "/");
    String fileName = ThothUtil.getPartBeforeFirst(ThothUtil.getFileName(spec), ",").trim();

    registerFolder(folder, getModification(fileName));
    List<String> list = folderContents.get(folder);
    if (!list.contains(fileName)) {
      list.add(fileName);

      String path = folder + "/" + fileName;
      modified.put(path, getModification(spec));
      lengths.put(path, getLength(spec));
    }
  }

  public void registerFolder(String folderSpec, long modificationDate) {
    folderSpec = ThothUtil.stripSuffix(folderSpec, "/");

    String folder = "";
    for (String part : folderSpec.split("/")) {
      folder += (folder.endsWith("/") ? "" : "/") + part;
      modified.put(folder, modificationDate);
      if (!folders.contains(folder)) {
        folders.add(folder);
        folderContents.put(folder, new ArrayList<String>());
      }
    }
  }

  @Override
  public FileHandle getFileHandle(String filename) {
    String path = ThothUtil.stripSuffix(ThothUtil.prefix(filename, "/"), "/");
    return new FileHandle(this, path);
  }

  // There is no reliable way of figuring out whether a resource on the classpath is a folder
  // or a file. Opening the folder as a resource might return an inputstream which is
  // the list of that folder.
  // So for now we return false if there is a folder with the same name, and then true
  // if the inputstream is not null
  public boolean isFile(FileHandle fileHandle) {
    if (fileHandle == null || fileHandle.getAbsolutePath() == null || fileHandle.getAbsolutePath().length() == 0)
      return false;
    if (folders.contains(fileHandle.getCanonicalPath()))
      return false;

    boolean isFile = false;
    try {
      InputStream is = getInputStream(fileHandle, false);
      if (is != null) {
        isFile = true;
        is.close();
      }
    } catch (IOException e) {// ignore
    }
    return isFile;
  }

  public boolean isDirectory(FileHandle fileHandle) {
    return isDirectory(fileHandle.getCanonicalPath());
  }

  private boolean isDirectory(String fileName) {
    return folders.contains(fileName);
  }

  public long lastModified(FileHandle fileHandle) {
    Long lng = modified.get(fileHandle.getCanonicalPath());
    return lng == null ? 0 : lng;
  }

  public long length(FileHandle fileHandle) {
    Long integer = lengths.get(fileHandle.getCanonicalPath());
    return integer == null ? 0 : integer;
  }

  public List<FileHandle> list(String folderName) {
    if (isDirectory(folderName)) {
      List<FileHandle> result = new ArrayList<FileHandle>();
      for (String name : folderContents.get(folderName)) {
        result.add(new FileHandle(this, folderName + "/" + name));
      }
      return result;
    }
    return null;
  }

  public void registerFiles(String descriptorFile) throws IOException {
    descriptorFile = ThothUtil.stripPrefix(descriptorFile, "/");
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(descriptorFile);
    if (is == null)
      throw new IllegalArgumentException("Descriptor " + descriptorFile + " not found on the classpath");

    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    String line = br.readLine();
    while (line != null) {
      if (line.trim().length() > 0)
        registerFile(line.trim());
      line = br.readLine();
    }
    br.close();
  }

  private long getLength(String fileName) {
    String arg = getArg(fileName, 1);
    return arg == null ? 0 : Long.parseLong(arg);
  }

  private String getArg(String fileName, int idx) {
    String[] parts = fileName.split("\\,");
    if (parts.length > idx && parts[idx].trim().length() > 0)
      return parts[idx].trim();
    return null;

  }

  private long getModification(String fileName) {
    String arg = getArg(fileName, 2);
    return arg == null ? 0 : Long.parseLong(arg);
  }

  public String[] list(FileHandle fileHandle) {
    List<FileHandle> list = list(fileHandle.getCanonicalPath());
    if (list == null)
      return null;
    String[] result = new String[list.size()];
    for (int i = 0; i < list.size(); i++)
      result[i] = list.get(i).getName();
    return result;
  }

  public FileHandle[] listFiles(FileHandle fileHandle) {
    List<FileHandle> list = list(fileHandle.getCanonicalPath());
    if (list == null)
      return null;
    return list.toArray(new FileHandle[list.size()]);
  }

  public InputStream getInputStream(FileHandle fileHandle) throws FileNotFoundException {
    return getInputStream(fileHandle, true);
  }

  public String getFileSystemRoot() {
    return fileSystemRoot;
  }

  @Override
  public boolean exists(FileHandle fileHandle) {
    return isFile(fileHandle) || isDirectory(fileHandle);
  }

  public InputStream getInputStream(FileHandle fileHandle, boolean throwOnError) throws FileNotFoundException {
    String suffix = ThothUtil.prefix(fileHandle.getCanonicalPath(), "/");
    String resourcePath = ThothUtil.stripPrefix(getFileSystemRoot() + suffix, "/");
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
    if (is == null && throwOnError)
      throw new FileNotFoundException(resourcePath);
    return is;
  }

}
