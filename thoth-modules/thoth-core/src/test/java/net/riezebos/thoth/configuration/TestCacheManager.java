package net.riezebos.thoth.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.CachemanagerException;

public class TestCacheManager extends CacheManager {

  Map<String, ByteArrayOutputStream> resources = new HashMap<String, ByteArrayOutputStream>();
  boolean mockReverseIndexes = true;

  public TestCacheManager(ContentManager contentManager) {
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

  public void setMockReverseIndexes(boolean mockReverseIndexes) {
    this.mockReverseIndexes = mockReverseIndexes;
  }

  @Override
  public Map<String, List<String>> getReverseIndex(boolean indirect) throws CachemanagerException {
    if (!mockReverseIndexes)
      return super.getReverseIndex(indirect);
    else
      return indirect ? getReverseIndexIndirect() : getReverseIndex();
  }

  protected Map<String, List<String>> getReverseIndex() {
    Map<String, List<String>> result = new HashMap<>();
    result.put("/main/subs/SubOne.md", Arrays.asList(new String[] {"/main/Main.md"}));
    result.put("/main/Main.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Second.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Second.book"}));
    result.put("/main/Third.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Fourth.md", Arrays.asList(new String[] {"/books/Second.book"}));
    return result;
  }

  protected Map<String, List<String>> getReverseIndexIndirect() {
    Map<String, List<String>> result = new HashMap<>();
    result.put("/main/subs/SubOne.md", Arrays.asList(new String[] {"/main/Main.md", "/books/Main.book"}));
    result.put("/main/Main.md", Arrays.asList(new String[] {"/books/Main.book"}));
    result.put("/main/Second.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Second.book"}));
    result.put("/main/Third.md", Arrays.asList(new String[] {"/books/Main.book", "/books/Main.book"}));
    result.put("/main/Fourth.md", Arrays.asList(new String[] {"/books/Second.book"}));
    return result;
  }

}
