package net.riezebos.thoth.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import net.riezebos.thoth.content.ContentManager;

public class TestCacheManager extends CacheManager {

  Map<String, ByteArrayOutputStream> resources = new HashMap<String, ByteArrayOutputStream>();

  protected TestCacheManager(ContentManager contentManager) {
    super(contentManager);
  }

  protected InputStream createInputStream(String resourcePath) throws FileNotFoundException {
    ByteArrayOutputStream byteArrayOutputStream = resources.get(resourcePath);
    if (byteArrayOutputStream == null)
      return null;

    ByteArrayInputStream bis = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    return bis;
  }

  protected OutputStream createOutputStream(String resourcePath) throws FileNotFoundException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    resources.put(resourcePath, bos);
    return bos;
  }

  protected void deleteFile(String fileName) {
    resources.remove(fileName);
  }
  
  public Map<String, ByteArrayOutputStream> getResources() {
    return resources;
  }
  
  public void setResources(Map<String, ByteArrayOutputStream> resources) {
    this.resources = resources;
  }

}
