package net.riezebos.thoth.markdown.filehandle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.util.ThothUtil;

public class ClasspathFileHandleFactory implements FileHandleFactory {

  List<String> folders = new ArrayList<String>();
  Map<String, List<String>> folderContents = new HashMap<String, List<String>>();
  Map<String, Long> modified = new HashMap<String, Long>();

  public void registerFile(String spec) {
    String folder = ThothUtil.getFolder(spec);
    String fileName = ThothUtil.getFileName(spec);

    registerFolder(folder);
    List<String> list = folderContents.get(folder);
    if (!list.contains(fileName)) {
      list.add(fileName);
      modified.put(spec, (list.size() + 1) * 10000L);
    }
  }

  public void registerFolder(String folderSpec) {
    folderSpec = ThothUtil.stripSuffix(folderSpec, "/");

    String folder = "";
    for (String part : folderSpec.split("/")) {
      folder += (folder.endsWith("/") ? "" : "/") + part;
      modified.put(folder, (folders.size() + 1) * 10000L);
      if (!folders.contains(folder)) {
        folders.add(folder);
        folderContents.put(folder, new ArrayList<String>());
        modified.put(folder, 10000L);
      }
    }
  }

  @Override
  public FileHandle createFileHandle(String filename) {
    return new ClasspathFileHandle(this, ThothUtil.stripSuffix(ThothUtil.prefix(filename, "/"), "/"));
  }

  public boolean exists(String filename) {
    return isDirectory(filename) || isFile(filename);
  }

  public boolean isFile(String filename) {
    filename = ThothUtil.getCanonicalPath(filename);
    String folder = ThothUtil.getFolder(filename);
    if (folder == null)
      return false;
    List<String> list = folderContents.get(folder);
    if (list == null)
      return false;
    // First check 'real' existence
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    InputStream is = contextClassLoader.getResourceAsStream(ThothUtil.stripPrefix(filename, "/"));
    if (is == null)
      return false;
    try {
      is.close();
    } catch (IOException e) {// ignore
    }
    return true;
  }

  public boolean isDirectory(String filename) {
    return folders.contains(ThothUtil.getCanonicalPath(filename));
  }

  public long lastModified(String filename) {
    Long lng = modified.get(filename);
    return lng == null ? 0 : lng;
  }

  public String[] list(String filename) {
    if (isDirectory(filename))
      return folderContents.get(filename).toArray(new String[0]);
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
}
