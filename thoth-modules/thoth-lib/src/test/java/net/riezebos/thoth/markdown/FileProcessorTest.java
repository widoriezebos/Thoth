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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.beans.Bookmark;
import net.riezebos.thoth.beans.BookmarkUsage;
import net.riezebos.thoth.markdown.filehandle.BasicFileSystem;

public class FileProcessorTest {

  @Test
  public void testGetMetaTagsInputStream() throws IOException {
    FileProcessor processor = new FileProcessor();
    InputStream is = new ByteArrayInputStream("meta1: value1\nmeta2:value2\nsomeother line\nThisisnotmeta:nono".getBytes("UTF-8"));
    Map<String, String> metaTags = processor.getMetaTags(is);
    assertEquals(2, metaTags.size());
    assertEquals("value1", metaTags.get("meta1"));
    assertEquals("value2", metaTags.get("meta2"));
  }

  @Test
  public void testReset() throws IOException {
    FileProcessor processor = new FileProcessor();
    processor.error("Some error");
    processor.reset();
    assertEquals(0, processor.getErrors().size());
    assertFalse(processor.hasErrors());

  }

  @Test
  public void testError() {
    FileProcessor processor = new FileProcessor();
    processor.error("Some error");
    assertTrue(processor.hasErrors());
  }

  @Test
  public void testReadLine() throws IOException {
    FileProcessor processor = new FileProcessor();
    InputStream is = new ByteArrayInputStream("meta1: value1   \nmeta2:value2   \nsomeother line\nThisisnotmeta:nono".getBytes("UTF-8"));
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line = processor.readLine(br);
    assertEquals("meta1: value1", line);
    processor.setStripTrailingWhitespace(false);
    line = processor.readLine(br);
    assertEquals("meta2:value2   ", line);
    int i = 0;
    do {
      line = processor.readLine(br);
      if (line != null)
        i++;
    } while (line != null);
    assertEquals(2, i);
  }

  @Test
  public void testExtractMetaInfo() throws UnsupportedEncodingException {
    FileProcessor processor = new FileProcessor();
    assertTrue(processor.extractMetaInfo("meta1: value1"));
    assertEquals("value1", processor.getMetaTags().get("meta1"));
    assertFalse(processor.extractMetaInfo("http://test.server.com"));
    assertFalse(processor.extractMetaInfo("ftp://test.server.com"));
    assertTrue(processor.extractMetaInfo("meta1: value2"));
    assertEquals("value1", processor.getMetaTags().get("meta1"));
  }

  @Test
  public void testUpdateTocFlag() {
    FileProcessor processor = new FileProcessor();
    processor.updateTocFlag("This is ordinary text");
    processor.updateTocFlag(null);
    assertFalse(processor.containsToc());
    processor.updateTocFlag("\\tableofcontents");
    assertTrue(processor.containsToc());

  }

  @Test
  public void testTranslateSoftLink() throws IOException {
    FileProcessor processor = new FileProcessor();
    assertEquals("nothing", processor.translateSoftLink("nothing"));
    InputStream is = new ByteArrayInputStream("tip=/some/path/tip.png\n~*=/datamodel/$1/Class.md".getBytes());
    processor.loadSoftLinks(is);

    assertEquals("/some/path/tip.png", processor.translateSoftLink(":tip"));
    assertEquals("/datamodel/Test/Class.md", processor.translateSoftLink(":~Test"));
  }

  @Test
  public void testRegisterBookMarkUsage() {
    FileProcessor processor = new FileProcessor();
    BookmarkUsage usage = new BookmarkUsage();
    processor.registerBookMarkUsage(usage);
    assertEquals(1, processor.getBookmarkUsages().size());
    assertEquals(usage, processor.getBookmarkUsages().get(0));
  }

  @Test
  public void testResolveLibraryPath() throws IOException {
    FileProcessor processor = new FileProcessor();
    processor.setLibrary("/path/to/library/");
    processor.setRootFolder("/path/to/library/then/someother/folder/");
    String resolved = processor.resolveLibraryPath("/in/the/lib");
    assertEquals("../../../in/the/lib", resolved);

    processor = new FileProcessor();
    processor.setLibrary("/path/to/library");
    processor.setRootFolder("/path/to/library/then/someother/folder/");
    resolved = processor.resolveLibraryPath("/in/the/lib");
    assertEquals("../../../in/the/lib", resolved);

    processor = new FileProcessor();
    processor.setLibrary("/path/to/library/");
    processor.setRootFolder("/path/to/library/then/someother/folder");
    resolved = processor.resolveLibraryPath("/in/the/lib");
    assertEquals("../../../in/the/lib", resolved);

    processor = new FileProcessor();
    processor.setLibrary("/path/to/library");
    processor.setRootFolder("/path/to/library/then/someother/folder");
    resolved = processor.resolveLibraryPath("/in/the/lib");
    assertEquals("../../../in/the/lib", resolved);
  }

  @Test
  public void testStepBack() throws IOException {
    FileProcessor processor = new FileProcessor();
    assertEquals(processor.stepBack("/in/some/folder/structure/deep", "/in/some"), "../../../");
    assertEquals(processor.stepBack("/in/some", "/in/some"), "");
  }

  @Test
  public void testCreateBookmark() {
    FileProcessor processor = new FileProcessor();
    processor.createBookmark("#Header", 0);
    Bookmark bookmark = processor.getBookmarks().get(0);
    assertEquals("1header", bookmark.getId());
    processor.createBookmark("##Header2", 0);
    bookmark = processor.getBookmarks().get(1);
    assertEquals("11header2", bookmark.getId());
  }

  @Test
  public void testAdjustHeaderLevel() {
    FileProcessor processor = new FileProcessor();
    assertEquals("###onedeep", processor.adjustHeaderLevel("#onedeep", 2));
    assertEquals("#onedeep", processor.adjustHeaderLevel("###onedeep", -2));
    assertEquals("#onedeep", processor.adjustHeaderLevel("###onedeep", -20));
  }

  @Test
  public void testUpdateHeaderCounters() {
    FileProcessor processor = new FileProcessor();
    processor.setMaxNumberingLevel(3);
    processor.updateHeaderCounters(1);
    assertEquals("1.0.0 ", processor.getHeaderCounter(3));
    processor.updateHeaderCounters(2);
    assertEquals("1.1.0 ", processor.getHeaderCounter(3));
    processor.updateHeaderCounters(3);
    assertEquals("1.1.1 ", processor.getHeaderCounter(3));
    processor.updateHeaderCounters(2);
    assertEquals("1.2.0 ", processor.getHeaderCounter(3));
    processor.updateHeaderCounters(1);
    assertEquals("2.0.0 ", processor.getHeaderCounter(3));
    processor.setMaxNumberingLevel(4);
    assertEquals("2.0.0.0 ", processor.getHeaderCounter(4));
  }

  @Test
  public void testIsValidBookmark() {
    FileProcessor processor = new FileProcessor();
    assertTrue(processor.isValidBookmark("abcd"));
    assertTrue(processor.isValidBookmark("1abcd"));
    assertFalse(processor.isValidBookmark("%1abcd"));
    assertFalse(processor.isValidBookmark("[1abcd"));
  }

  @Test
  public void testGetHeaderLevel() {
    FileProcessor processor = new FileProcessor();
    assertEquals(new Integer(1), processor.getHeaderLevel(new String[] {"filename", "1"}, "filename, 1"));
    assertEquals(null, processor.getHeaderLevel(new String[] {"filename"}, "filename"));
    assertFalse(processor.hasErrors());
    assertEquals(null, processor.getHeaderLevel(new String[] {"filename", "invalid"}, "filename, invalid"));
    assertTrue(processor.hasErrors());
  }

  @Test
  public void testGetFolderName() {
    FileProcessor processor = new FileProcessor();
    assertEquals("/this/is/thefolder/", processor.getFolderName("/this/is/thefolder/file.txt"));
    assertEquals("", processor.getFolderName("thefile"));
  }

  @Test
  public void testComment() throws UnsupportedEncodingException {
    FileProcessor processor = new FileProcessor();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream pw = new PrintStream(bos);

    processor.comment(pw, "Test");
    pw.flush();

    assertEquals("\n[//]: # \"Test\"\n", new String(bos.toByteArray(), "UTF-8"));
  }

  @Test
  public void testCreateToc() {
    FileProcessor processor = new FileProcessor();
    processor.createBookmark("#Chapter1", 0);
    processor.createBookmark("##Paragraph11", 0);
    processor.createBookmark("##Paragraph12", 0);
    processor.createBookmark("#Chapter2", 0);
    processor.createBookmark("##Paragraph21", 0);
    processor.createBookmark("#Chapter3", 0);
    String doc = processor.createToc("Title\n\\tableofcontents\n\nFirst line");

    String expected = "Title\n" + //
        "<tableofcontents>\n" + //
        "###[1 Chapter1](#1chapter1)\n" + //
        "\n" + //
        "  - [1.1 Paragraph11](#11paragraph11)\n" + //
        "  - [1.2 Paragraph12](#12paragraph12)\n" + //
        "\n" + //
        "###[2 Chapter2](#2chapter2)\n" + //
        "\n" + //
        "  - [2.1 Paragraph21](#21paragraph21)\n" + //
        "\n" + //
        "###[3 Chapter3](#3chapter3)\n" + //
        "\n" + //
        "</tableofcontents>\n" + //
        "\n" + //
        "First line";

    assertEquals(expected, doc);
  }

  @Test
  public void testFixFolderSpec() {
    FileProcessor processor = new FileProcessor();
    assertEquals("d:/some/windows/folderspec/", processor.fixFolderSpec("d:\\some\\windows\\folderspec"));
  }

  @Test
  public void testGetArgumentsMap() {
    Map<String, String> argumentsMap = FileProcessor.getArgumentsMap(new String[] {"-file", "filename", "-flag", "true", "-toggle", "-toggle2"});
    assertEquals("filename", argumentsMap.get("file"));
    assertEquals("true", argumentsMap.get("flag"));
    assertEquals(null, argumentsMap.get("toggle"));
    assertTrue(argumentsMap.containsKey("toggle"));
    assertTrue(argumentsMap.containsKey("toggle2"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetArgumentsMapFail() {
    FileProcessor.getArgumentsMap(new String[] {"file", "filename", "-flag", "true", "-toggle"});
  }

  @Test
  public void testSetLineNumber() throws IOException {
    FileProcessor processor = new FileProcessor();
    processor.setLibrary("/some/library");
    processor.startNewFile("/some/library/file.md");
    processor.setLineNumber(1);
    processor.startNewFile("/file2.md");
    processor.setLineNumber(2);
    assertEquals(2, processor.getCurrentLineInfo().getLine());
    assertEquals("/file2.md", processor.getCurrentLineInfo().getFile());
    processor.endFile();
    assertEquals(1, processor.getCurrentLineInfo().getLine());
    assertEquals("/file.md", processor.getCurrentLineInfo().getFile());

  }

  @Test
  public void testCanonicalize() {
    FileProcessor processor = new FileProcessor();
    assertEquals(FileProcessor.STDIN, processor.canonicalize(FileProcessor.STDIN));
    String canonicalized = processor.canonicalize("one/sub1/../two");
    assertTrue(canonicalized.endsWith("one/two/"));
  }

  @Test
  public void testValidate() {
    FileProcessor processor = new FileProcessor();
    processor.createBookmark("#Header", 0);
    processor.createBookmark("#Header2", 0);

    processor.registerBookMarkUsage(new BookmarkUsage("header2"));
    processor.registerBookMarkUsage(new BookmarkUsage("2header2"));
    processor.validate();
    assertFalse(processor.hasErrors());
    processor.registerBookMarkUsage(new BookmarkUsage("wrongheader"));
    processor.validate();
    assertTrue(processor.hasErrors());
  }

  @Test
  public void testMakeRelativeToLibrary() throws IOException {

    FileProcessor processor = new FileProcessor();
    File libPath = File.createTempFile("lib", "");
    libPath.deleteOnExit();
    String libraryPath = processor.getFolderName(libPath.getAbsolutePath());
    File libFolder = new File(libraryPath);
    String someFile = libraryPath + "/some/other/path/file.md";

    BasicFileSystem fs = new BasicFileSystem();
    processor.setLibrary(libFolder.getAbsolutePath());
    String relative = processor.makeRelativeToLibrary(fs.getFileHandle(someFile));
    assertEquals("some/other/path/file.md", relative);
    assertFalse(processor.hasErrors());
    processor.makeRelativeToLibrary(fs.getFileHandle("/outside/the/library"));
    assertTrue(processor.hasErrors());
  }

  @Test
  public void testInCodeBlock() {
    FileProcessor processor = new FileProcessor();
    assertTrue(processor.inCodeBlock("\tsomecode"));
    assertTrue(processor.inCodeBlock("    somecode"));
    assertFalse(processor.inCodeBlock("  somecode"));
    assertFalse(processor.inCodeBlock("somecode"));

  }

  @Test
  public void testAsInt() {
    FileProcessor processor = new FileProcessor();
    assertEquals(1, processor.asInt("1"));
    assertFalse(processor.hasErrors());
    assertEquals(0, processor.asInt("invalid"));
    assertTrue(processor.hasErrors());
  }
}
