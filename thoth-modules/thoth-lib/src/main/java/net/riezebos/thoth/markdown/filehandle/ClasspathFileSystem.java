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
package net.riezebos.thoth.markdown.filehandle;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.riezebos.thoth.util.ThothUtil;

public class ClasspathFileSystem implements FileSystem {

  private String fileSystemRoot;
  private Set<String> folders = new HashSet<String>();
  private Map<String, Set<String>> folderFiles = new HashMap<String, Set<String>>();
  private Map<String, Set<String>> subFolders = new HashMap<String, Set<String>>();
  private Map<String, Long> modified = new HashMap<String, Long>();
  private Map<String, Long> lengths = new HashMap<String, Long>();

  public ClasspathFileSystem() {
    this(null);
  }

  public ClasspathFileSystem(String fileSystemRoot) {
    if (fileSystemRoot == null)
      fileSystemRoot = "";
    setFileSystemRoot(ThothUtil.stripSuffix(fileSystemRoot, "/"));
  }

  public void registerFile(String spec, long modified, long length) {
    String folder = ThothUtil.absoluteFolder(ThothUtil.getFolder(spec));
    String fileName = ThothUtil.getPartBeforeFirst(ThothUtil.getFileName(spec), ",").trim();

    registerFolder(folder, getModification(fileName));
    Set<String> list = folderFiles.get(ThothUtil.absoluteFolder(folder));
    if (!list.contains(fileName)) {
      list.add(fileName);

      String path = ThothUtil.prefix(folder, "/") + fileName;
      setModified(path, modified);
      setLength(path, length);
    }
  }

  public void registerFolder(String folderSpec, long modificationDate) {
    folderSpec = ThothUtil.stripSuffix(folderSpec, "/");

    String folder = "";
    Set<String> parentFolders = null;
    for (String part : folderSpec.split("/")) {
      folder += (folder.endsWith("/") ? "" : "/") + part;

      String absoluteFolder = ThothUtil.absoluteFolder(folder);

      if (parentFolders != null)
        parentFolders.add(part);

      Set<String> nestedFolders = subFolders.get(absoluteFolder);
      if (nestedFolders == null) {
        nestedFolders = new HashSet<String>();
        subFolders.put(absoluteFolder, nestedFolders);
      }

      setModified(absoluteFolder, modificationDate);
      if (!folders.contains(absoluteFolder)) {
        folders.add(absoluteFolder);
        folderFiles.put(absoluteFolder, new HashSet<String>());
      }
      parentFolders = nestedFolders;
    }
  }

  protected void setModified(String path, long modificationDate) {
    modified.put(path, modificationDate);
  }

  protected void setLength(String path, long length) {
    lengths.put(path, length);
  }

  @Override
  public FileHandle getFileHandle(String filename) {
    String canonicalPath = ThothUtil.getCanonicalPath(filename);
    String path = ThothUtil.stripSuffix(ThothUtil.prefix(canonicalPath, "/"), "/");
    if (path.equals(""))
      path = "/";
    return new FileHandle(this, path);
  }

  // There is no reliable way of figuring out whether a resource on the classpath is a folder
  // or a file. Opening the folder as a resource might return an inputstream which is
  // the list of that folder.
  // So for now we return false if there is a folder with the same name, and then true
  // if the inputstream is not null
  @Override
  public boolean isFile(FileHandle fileHandle) {
    if (fileHandle == null || fileHandle.getAbsolutePath() == null || fileHandle.getAbsolutePath().length() == 0)
      return false;
    if (folders.contains(ThothUtil.absoluteFolder(fileHandle.getCanonicalPath())))
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

  @Override
  public boolean isDirectory(FileHandle fileHandle) {
    return isDirectory(fileHandle.getCanonicalPath());
  }

  @Override
  public boolean delete(FileHandle fileHandle) {
    return false;
  }

  private boolean isDirectory(String fileName) {
    return folders.contains(fileName) || folders.contains(ThothUtil.absoluteFolder(fileName));
  }

  @Override
  public long lastModified(FileHandle fileHandle) {
    String canonicalPath = fileHandle.getCanonicalPath();
    Long lng = modified.get(canonicalPath);
    return lng == null ? 0 : lng;
  }

  @Override
  public long length(FileHandle fileHandle) {
    Long integer = lengths.get(fileHandle.getCanonicalPath());
    return integer == null ? 0 : integer;
  }

  public List<FileHandle> list(String folderName) {
    if (isDirectory(folderName)) {
      List<FileHandle> result = new ArrayList<FileHandle>();
      for (String name : folderFiles.get(ThothUtil.absoluteFolder(folderName))) {
        result.add(new FileHandle(this, ThothUtil.suffix(folderName, "/") + name));
      }
      for (String name : subFolders.get(ThothUtil.absoluteFolder(folderName))) {
        result.add(new FileHandle(this, ThothUtil.suffix(folderName, "/") + name));
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
      String fileNameSpec = line.trim();
      if (fileNameSpec.length() > 0) {
        long modification = getModification(fileNameSpec);
        long length = getLength(fileNameSpec);
        String fileName = ThothUtil.prefix(ThothUtil.getPartBeforeFirst(fileNameSpec, ",").trim(), "/");
        fileName = ThothUtil.prefix(fileName, "/");
        if (fileName.startsWith(getFileSystemRoot()))
          fileName = fileName.substring(getFileSystemRoot().length());

        registerFile(fileName, modification, length);
      }
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

  @Override
  public String[] list(FileHandle fileHandle) {
    List<FileHandle> list = list(fileHandle.getCanonicalPath());
    if (list == null)
      return null;
    String[] result = new String[list.size()];
    for (int i = 0; i < list.size(); i++)
      result[i] = list.get(i).getName();
    return result;
  }

  @Override
  public FileHandle[] listFiles(FileHandle fileHandle) {
    List<FileHandle> list = list(fileHandle.getCanonicalPath());
    if (list == null)
      return null;
    return list.toArray(new FileHandle[list.size()]);
  }

  @Override
  public InputStream getInputStream(FileHandle fileHandle) throws IOException {
    return getInputStream(fileHandle, true);
  }

  public String getFileSystemRoot() {
    return fileSystemRoot;
  }

  protected void setFileSystemRoot(String fileSystemRoot) {
    this.fileSystemRoot = fileSystemRoot;
  }

  @Override
  public boolean exists(FileHandle fileHandle) {
    return isFile(fileHandle) || isDirectory(fileHandle);
  }

  public InputStream getInputStream(FileHandle fileHandle, boolean throwOnError) throws IOException {
    String suffix = ThothUtil.prefix(fileHandle.getCanonicalPath(), "/");
    if (denied(suffix))
      return null;

    String resourcePath = ThothUtil.stripPrefix(getFileSystemRoot() + suffix, "/");

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
    if (is == null && throwOnError)
      throw new FileNotFoundException(resourcePath);
    return is;
  }

  @Override
  public OutputStream getOutputStream(FileHandle fileHandle) throws IOException {
    throw new IOException("getOutputStream is not supported for ClasspathFileSystem");
  }

  protected boolean denied(String path) {
    return (path.startsWith("../") || path.startsWith("/../"));
  }

  public Set<String> getFolders() {
    return folders;
  }

  public Set<String> getFolderContents(String folderName) {
    return folderFiles.get(ThothUtil.absoluteFolder(folderName));
  }

  @Override
  public Set<String> getCreatedFiles() {
    return new HashSet<String>();
  }
}
