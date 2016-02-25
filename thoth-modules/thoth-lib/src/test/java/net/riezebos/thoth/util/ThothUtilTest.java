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
package net.riezebos.thoth.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ThothUtilTest {

  @Test
  public void testConstruct() {
    new ThothUtil();
  }

  @Test
  public void testGetArgumentsMap() {
    Map<String, String> argumentsMap = ThothUtil.getArgumentsMap(new String[] {"-file", "filename", "-flag", "true", "-toggle", "-toggle2"});
    assertEquals("filename", argumentsMap.get("file"));
    assertEquals("true", argumentsMap.get("flag"));
    assertEquals(null, argumentsMap.get("toggle"));
    assertTrue(argumentsMap.containsKey("toggle"));
    assertTrue(argumentsMap.containsKey("toggle2"));
  }

  @Test
  public void testGetArgumentsMixed() {
    String[] args = new String[] {"file", "filename", "-flag", "true", "somethingelse", "-toggle"};
    Map<String, String> argumentsMap = ThothUtil.getArgumentsMap(args);
    assertNull(argumentsMap.get("file"));
    assertEquals("true", argumentsMap.get("flag"));
    assertEquals(null, argumentsMap.get("toggle"));
    assertTrue(argumentsMap.containsKey("toggle"));
    
    List<String> argumentsList = ThothUtil.getArgumentsList(args);
    assertEquals(3, argumentsList.size());
    assertTrue(argumentsList.contains("file"));
    assertTrue(argumentsList.contains("filename"));
    assertTrue(argumentsList.contains("somethingelse"));
  }

  @Test
  public void testTidyRelativePath() {
    assertNull(ThothUtil.tidyRelativePath(null));
    assertEquals("", ThothUtil.tidyRelativePath(""));
    assertEquals("", ThothUtil.tidyRelativePath("/"));
    assertEquals("some/other/path", ThothUtil.tidyRelativePath("/some/other/path"));
  }

  @Test
  public void testNormalSlashes() {
    assertNull(ThothUtil.normalSlashes(null));
    assertEquals("/some/other/path", ThothUtil.normalSlashes("/some/other/path"));
    assertEquals("/some/other/path", ThothUtil.normalSlashes("\\some\\other\\path"));
  }

  @Test
  public void testGetNameOnly() {
    assertNull(ThothUtil.getNameOnly(null));
    assertEquals("path", ThothUtil.getNameOnly("/some/other/path"));
    assertEquals("", ThothUtil.getNameOnly("/some/other/path/"));
    assertEquals("file", ThothUtil.getNameOnly("/some/other/path/file"));
    assertEquals("file", ThothUtil.getNameOnly("/some/other/path/file.txt"));
  }

  @Test
  public void testGetFileName() {
    assertNull(ThothUtil.getFileName(null));
    assertEquals("path", ThothUtil.getFileName("/some/other/path"));
    assertEquals("", ThothUtil.getFileName("/some/other/path/"));
    assertEquals("file", ThothUtil.getFileName("/some/other/path/file"));
    assertEquals("file.txt", ThothUtil.getFileName("/some/other/path/file.txt"));
    assertEquals("file.txt", ThothUtil.getFileName("file.txt"));
  }

  @Test
  public void testWrapWithNewLines() {
    assertNull(ThothUtil.wrapWithNewLines(null));
    assertArrayEquals("\na\n".toCharArray(), ThothUtil.wrapWithNewLines("a".toCharArray()));
  }

  @Test
  public void testGetFolder() {
    assertNull(ThothUtil.getFolder(null));
    assertEquals("path", ThothUtil.getFolder("path"));
    assertEquals("/some/other", ThothUtil.getFolder("/some/other/path"));
    assertEquals("/some/other/path", ThothUtil.getFolder("/some/other/path/"));
    assertEquals("/some/other/path", ThothUtil.getFolder("/some/other/path/file"));
    assertEquals("/some/other/path", ThothUtil.getFolder("/some/other/path/file.txt"));
  }

  @Test
  public void testStripSuffix() {
    assertNull(ThothUtil.stripSuffix(null, ".txt"));
    assertNull(ThothUtil.stripSuffix(null, null));
    assertEquals("/some/other/path/file", ThothUtil.stripSuffix("/some/other/path/file", null));
    assertEquals("/some/other/path/file", ThothUtil.stripSuffix("/some/other/path/file", ".txt"));
    assertEquals("/some/other/path/file", ThothUtil.stripSuffix("/some/other/path/file.txt", ".txt"));
  }

  @Test
  public void testEncodeBookmark() {
    assertNull(ThothUtil.encodeBookmark(null, true));
    assertEquals("", ThothUtil.encodeBookmark("", true));
    assertEquals("a1bc", ThothUtil.encodeBookmark("a-1 #B C", true));
    assertEquals("a1BC", ThothUtil.encodeBookmark("a-1 #B C", false));
  }

  @Test
  public void testReplaceKeywords() {

    Map<String, Object> args = new HashMap<String, Object>();
    args.put("one", 1);
    args.put("two", "2");
    args.put("three", "$1");
    args.put("four", null);

    assertNull(ThothUtil.replaceKeywords(null, args));
    assertEquals("test 1 and 2", ThothUtil.replaceKeywords("test ${one} and ${two}", args));
    assertEquals("test 1 and 22", ThothUtil.replaceKeywords("test ${one} and ${two}${two}", args));
    assertEquals("test 1 and 22", ThothUtil.replaceKeywords("test ${one} and ${two}${two}", args));
    assertEquals("test $1", ThothUtil.replaceKeywords("test ${three}", args));
    assertEquals("test ", ThothUtil.replaceKeywords("test ${four}", args));
  }

  @Test
  public void testRegExpescapeString() {
    assertEquals("\\[\\^a-9\\]\\.\\*\\?", ThothUtil.regExpescape("[^a-9].*?"));
  }

  @Test
  public void testExtractKeyswords() {
    List<String> keywords = ThothUtil.extractKeyswords("There should be ${1} and ${another} keyword");
    assertArrayEquals(new String[] {"1", "another"}, keywords.toArray(new String[0]));

    keywords = ThothUtil.extractKeyswords("There should be no keyword");
    assertArrayEquals(new String[0], keywords.toArray(new String[0]));
    assertArrayEquals(new String[0], ThothUtil.extractKeyswords(null).toArray(new String[0]));
  }

  @Test
  public void testSpecAsRegExp() {
    assertEquals("(.*?)\\.(.*?)", ThothUtil.specAsRegExp("*.*").toString());
    assertEquals("(.*?)\\.png", ThothUtil.specAsRegExp("*.png").toString());
  }

  @Test
  public void testTokenize() {
    assertArrayEquals(new String[0], ThothUtil.tokenize(null).toArray(new String[0]));
    assertArrayEquals(new String[0], ThothUtil.tokenize("").toArray(new String[0]));
    assertArrayEquals(new String[] {"one", "two", "three"}, ThothUtil.tokenize("one, two, three").toArray(new String[0]));
    assertArrayEquals(new String[] {"one", "two", "three"}, ThothUtil.tokenize("one, two, three,").toArray(new String[0]));
  }

  @Test
  public void testSort() {
    List<String> lst = new ArrayList<String>();
    lst.add("4");
    lst.add("1");
    lst.add("3");
    lst.add("2");
    assertArrayEquals(new String[] {"1", "2", "3", "4"}, ThothUtil.sort(lst).toArray(new String[0]));
  }

  @Test
  public void testStripNumericPrefix() {
    assertNull(ThothUtil.stripNumericPrefix(null));
    assertEquals("abc", ThothUtil.stripNumericPrefix("abc"));
    assertEquals("abc", ThothUtil.stripNumericPrefix("12abc"));
    assertEquals("abc", ThothUtil.stripNumericPrefix("12 abc"));
    assertEquals("", ThothUtil.stripNumericPrefix("12"));
  }

  @Test
  public void testGetPartBeforeFirst() {
    assertNull(ThothUtil.getPartBeforeFirst(null, null));
    assertNull(ThothUtil.getPartBeforeFirst(null, "/"));
    assertEquals("some", ThothUtil.getPartBeforeFirst("some/other/part", "/"));
    assertEquals("some/other/part", ThothUtil.getPartBeforeFirst("some/other/part", "%"));
  }

  @Test
  public void testGetPartAfterFirst() {
    assertNull(ThothUtil.getPartAfterFirst(null, null));
    assertNull(ThothUtil.getPartAfterFirst(null, "/"));
    assertEquals("other/part", ThothUtil.getPartAfterFirst("some/other/part", "/"));
    assertEquals("", ThothUtil.getPartAfterFirst("some/other/part", "%"));
  }

  @Test
  public void testGetPartAfterLast() {
    assertNull(ThothUtil.getPartAfterLast(null, null));
    assertNull(ThothUtil.getPartAfterLast(null, "/"));
    assertEquals("part", ThothUtil.getPartAfterLast("some/other/part", "/"));
    assertEquals("some/other/part", ThothUtil.getPartAfterLast("some/other/part", "%"));
  }

  @Test
  public void testGetPartBeforeLast() {
    assertNull(ThothUtil.getPartBeforeLast(null, null));
    assertNull(ThothUtil.getPartBeforeLast(null, "/"));
    assertEquals("some/other", ThothUtil.getPartBeforeLast("some/other/part", "/"));
    assertEquals("some/other/part", ThothUtil.getPartBeforeLast("some/other/part", "%"));
  }

  @Test
  public void testStripPrefix() {
    assertNull(ThothUtil.stripPrefix(null, null));
    assertNull(ThothUtil.stripPrefix(null, "a"));
    assertEquals("b", ThothUtil.stripPrefix("b", "a"));
    assertEquals("/other/part", ThothUtil.stripPrefix("some/other/part", "some"));
  }

  @Test
  public void testGetExtension() {
    assertNull(ThothUtil.getExtension(null));
    assertNull(ThothUtil.getExtension("/some/folder/file"));
    assertEquals("txt", ThothUtil.getExtension("/some/folder/file.txt"));
  }

  @Test
  public void testPrefix() {
    assertEquals("/", ThothUtil.prefix(null, "/"));
    assertEquals("/", ThothUtil.prefix("", "/"));
    assertEquals("/", ThothUtil.prefix("/", "/"));
    assertEquals("/a", ThothUtil.prefix("/a", "/"));
    assertEquals("/a", ThothUtil.prefix("a", "/"));
  }
}
