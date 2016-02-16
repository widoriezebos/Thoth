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
package net.riezebos.thoth.markdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.markdown.util.ProcessorError;

public class IncludeProcessorTest {

  private static final String RESOURCE_BASE = "net/riezebos/thoth/markdown/";

  @Test
  public void testIncludeImages() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setLibrary("/net/riezebos/thoth/");
    includeProcessor.setFileSystem(factory);
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/IncludeImagesTest.md");
    String result = includeProcessor.execute(handle);
    assertTrue(result.indexOf("![](images/img1.txt)") != -1);
    assertTrue(result.indexOf("![](images/img2.txt)") != -1);
    assertTrue(result.indexOf("![](images/img3.txt)") != -1);
    assertTrue(result.indexOf("![](../markdown/images/img1.txt)") != -1);

    // Check generated headers of the images
    assertTrue(result.indexOf("#2 img1") != -1);
    assertTrue(result.indexOf("#3 img2") != -1);
  }

  @Test
  public void testIncludeImagesFail() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setLibrary("/net/riezebos/thoth/");
    includeProcessor.setFileSystem(factory);
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/IncludeImagesFail.md");
    includeProcessor.execute(handle);
    List<ProcessorError> errors = includeProcessor.getErrors();
    assertTrue(errors.get(0).getErrorMessage().indexOf("spec invalid") != -1);
  }

  @Test
  public void testIncludeImages2() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setLibrary("/net/riezebos/thoth/");
    includeProcessor.setFileSystem(factory);
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/IncludeImagesTest2.md");
    String result = includeProcessor.execute(handle);

    // Absolute link must be relativized now; so:
    assertTrue(result.indexOf("![](../markdown/images/img2.txt)") != -1);
  }

  @Test
  public void testIncludeCode() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setFileSystem(factory);
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/CodeTest.md");
    String result = includeProcessor.execute(handle);
    assertTrue(result.indexOf("\tof code") != -1);
    assertTrue(result.indexOf("\twhen included") != -1);
  }

  @Test
  public void testIncludeCodeFail() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setFileSystem(factory);
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/CodeFailTest.md");
    includeProcessor.execute(handle);
    List<ProcessorError> errors = includeProcessor.getErrors();
    assertTrue(errors.get(0).getErrorMessage().indexOf("not found") != -1);
  }

  @Test
  public void testIncludeCodeAbsolute() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setLibrary("/net/riezebos/thoth/");
    includeProcessor.setFileSystem(factory);
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/CodeTest2.md");
    String result = includeProcessor.execute(handle);
    assertTrue(result.indexOf("\tof code") != -1);
    assertTrue(result.indexOf("\twhen included") != -1);
  }

  @Test
  public void testIncludeLevels() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setFileSystem(factory);
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/IncludeTest1.md");
    String result = includeProcessor.execute(handle);
    // Must have a level two header because of indent level 1 specified at include:
    assertTrue(result.indexOf("##1") != -1);
    long latestIncludeModificationDate = includeProcessor.getLatestIncludeModificationDate();
    assertEquals(80000, latestIncludeModificationDate);
  }

  @Test
  public void testToc() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setFileSystem(factory);
    includeProcessor.loadSoftLinks(getResource("softlinks.properties"));
    String source = includeProcessor.execute("IncludeProcessor.md", getResource("IncludeProcessor.md"));
    assertTrue(source.indexOf("<tableofcontents>") != -1);
    assertTrue(source.indexOf("###[1 Chapter one](#1chapterone)") != -1);

    includeProcessor.setSoftlinkFile("softlinks.properties");
    includeProcessor.reset();

    source = includeProcessor.execute(null, getResource("IncludeProcessor.md"));
    assertTrue(source.indexOf("###[1 Chapter one](#1chapterone)") != -1);

    includeProcessor.reset();
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/IncludeProcessor.md");
    source = includeProcessor.execute(handle);
    assertTrue(source.indexOf("###[1 Chapter one](#1chapterone)") != -1);
  }

  @Test
  public void testCyclic() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setFileSystem(factory);
    FileHandle cyclic = factory.getFileHandle("/net/riezebos/thoth/markdown/Cyclic.md");
    includeProcessor.execute(cyclic);
    List<ProcessorError> errors = includeProcessor.getErrors();
    assertTrue(errors.get(0).getErrorMessage().indexOf("include depth") != -1);
  }

  @Test
  public void testIncludeAbs() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setLibrary("/net/riezebos/thoth/");
    includeProcessor.setFileSystem(factory);
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/IncludeTestAbs.md");
    String result = includeProcessor.execute(handle);

    // Absolute link must be relativized now; so:
    assertTrue(result.indexOf("Some other text") != -1);
  }

  @Test
  public void testIncludeFail() throws IOException {
    ClasspathFileSystem factory = getFileHandleFactory();
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setLibrary("/net/riezebos/thoth/markdown");
    includeProcessor.setSoftlinkFile("softlinks.properties");
    includeProcessor.setFileSystem(factory);
    FileHandle handle = factory.getFileHandle("/net/riezebos/thoth/markdown/IncludeTestFail.md");
    includeProcessor.execute(handle);
    List<ProcessorError> errors = includeProcessor.getErrors();
    assertTrue(errors.get(0).getErrorMessage().indexOf("Include not found") != -1);

    includeProcessor.reset();
    handle = factory.getFileHandle("/net/riezebos/thoth/markdown/InvalidSoftlink.md");
    includeProcessor.execute(handle);
    errors = includeProcessor.getErrors();
    assertTrue(errors.get(0).getErrorMessage().indexOf("Include not found") != -1);
    assertTrue(errors.get(0).getErrorMessage().indexOf("translated by softlink") != -1);

  }

  protected ClasspathFileSystem getFileHandleFactory() throws IOException {
    ClasspathFileSystem factory = new ClasspathFileSystem();
    factory.registerFiles("/net/riezebos/thoth/resources.lst");
    return factory;
  }

  protected InputStream getResource(String testResource) {
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_BASE + testResource);
    return in;
  }

  @Test
  public void testUsage() throws IOException {
    IncludeProcessor includeProcessor = new IncludeProcessor();
    String outMessage = execMain(includeProcessor, new String[] {"-help"});
    assertTrue(outMessage.indexOf("Usage:") != -1);

    includeProcessor.setFileSystem(getFileHandleFactory());
    includeProcessor.setLibrary("/net/riezebos/thoth/");
    outMessage = execMain(includeProcessor,
        new String[] {"-file", "/net/riezebos/thoth/markdown/IncludeProcessor.md"//
            , "-library", "/net/riezebos/thoth"//
            , "-softlinkFile", "softlinks.properties"//
            , "-numbering", "0"//
    });
    assertTrue(outMessage.indexOf("##Paragraph one of chapter one") != -1);
  }

  @Test
  public void testTarget() throws IOException {
    IncludeProcessor includeProcessor = new IncludeProcessor();
    includeProcessor.setFileSystem(getFileHandleFactory());
    includeProcessor.setLibrary("/net/riezebos/thoth/");
    File tempFile = File.createTempFile("tmp", "tmp");
    tempFile.deleteOnExit();
    execMain(includeProcessor,
        new String[] {"-file", "/net/riezebos/thoth/markdown/IncludeProcessor.md"//
            , "-library", "/net/riezebos/thoth"//
            , "-softlinkFile", "softlinks.properties"//
            , "-target", tempFile.getAbsolutePath()//
    });
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile), "UTF-8"));
    String line = br.readLine();
    assertTrue(line.equals("MARKDOWN TESTS"));
    br.close();
  }

  private String execMain(IncludeProcessor includeProcessor, String[] args) throws FileNotFoundException, IOException, UnsupportedEncodingException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bos, true);
    includeProcessor.setOut(out);
    includeProcessor.execute(args);
    out.close();
    String outMessage = new String(bos.toByteArray(), "UTF-8");
    return outMessage;
  }

}
