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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.riezebos.thoth.beans.Bookmark;
import net.riezebos.thoth.beans.BookmarkUsage;
import net.riezebos.thoth.markdown.filehandle.BasicFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.markdown.filehandle.FileSystem;
import net.riezebos.thoth.markdown.util.LineInfo;
import net.riezebos.thoth.markdown.util.ProcessorError;
import net.riezebos.thoth.markdown.util.SoftLinkTranslation;
import net.riezebos.thoth.util.ThothUtil;

/**
 * @author wido
 */
public class FileProcessor {

  private static final String SOFTLINKS_PROPERTIES_DFLT = "softlinks.properties";

  public static final String TABLEOFCONTENTS_TAG = "tableofcontents";

  protected static int DEFAULT_NUMBERING_LEVEL = 3;
  protected static final String STDIN = "stdin";

  private List<Bookmark> bookmarks = new ArrayList<Bookmark>();
  private int maxNumberingLevel = DEFAULT_NUMBERING_LEVEL;
  private String library;
  private String rootFolder;
  private List<ProcessorError> errors = new ArrayList<ProcessorError>();
  private Stack<LineInfo> currentInfo = new Stack<LineInfo>();
  private boolean containsToc = false;
  private HashMap<String, String> metaTags = new HashMap<String, String>();
  private int[] headerCounters = new int[20];
  private boolean addComments = true;
  private boolean stripTrailingWhitespace = true;
  private String softLinkFileName = null;
  private Map<String, String> softLinkMappings = new HashMap<String, String>();
  private List<SoftLinkTranslation> softLinkTranslations = new ArrayList<SoftLinkTranslation>();
  private List<BookmarkUsage> bookmarkUsages = new ArrayList<BookmarkUsage>();
  private FileSystem fileSystem = new BasicFileSystem();
  private PrintStream out = System.out;

  public FileProcessor() {
  }

  public void setFileSystem(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  /**
   * Scans the input stream for meta tags. Includes are NOT processed. if you want that to happen you should use and IncludeProcessor. Meta tags in the input
   * stream should have the following format: <!--meta key=value-->
   *
   * @param is
   * @return a map with key value pairs.
   * @throws IOException
   */
  public Map<String, String> getMetaTags(InputStream is) throws IOException {
    reset();
    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    String line = readLine(br);
    boolean inMetaDataSection = true;
    while (line != null && inMetaDataSection) {
      inMetaDataSection = extractMetaInfo(line);
      line = readLine(br);
    }
    br.close();
    return metaTags;
  }

  protected void reset() throws IOException {
    containsToc = false;
    errors = new ArrayList<ProcessorError>();
    currentInfo = new Stack<LineInfo>();
    metaTags = new HashMap<String, String>();
    headerCounters = new int[20];
    bookmarkUsages = new ArrayList<BookmarkUsage>();
    loadSoftLinks();
  }

  protected void error(String message) {
    LineInfo currentLineInfo = getCurrentLineInfo();
    errors.add(new ProcessorError(currentLineInfo, message));
  }

  protected String readLine(BufferedReader br) throws IOException {
    String line = br.readLine();
    if (line != null && this.stripTrailingWhitespace) {
      int lastChar = line.length() - 1;
      int idx = lastChar;
      while (idx > 0 && Character.isWhitespace(line.charAt(idx)))
        idx--;
      if (idx < lastChar && idx >= 0)
        line = line.substring(0, idx + 1);
    }
    return line;
  }

  protected boolean extractMetaInfo(String line) {
    int idx = line.indexOf(":");
    if (idx != -1) {
      boolean consider = Character.isLetter(line.trim().charAt(0));
      consider &= !line.trim().startsWith("http://");
      consider &= !line.trim().startsWith("ftp://");
      if (consider) {
        String key = line.substring(0, idx).trim();
        if (key.indexOf(' ') != -1)
          return false;
        String value = line.substring(idx + 1).trim();
        if (!this.metaTags.containsKey(key) && value.length() > 0)
          this.metaTags.put(key, value);
        return true;
      }
    }
    return false;
  }

  protected void updateTocFlag(String line) {
    if (line != null) {
      containsToc |= (line.indexOf("\\tableofcontents") != -1);
    }
  }

  protected String translateSoftLink(String link) {
    if (link.startsWith(":")) {
      String key = link.substring(1);
      Object value = getSoftLinkMappings().get(key.toLowerCase());
      if (value != null)
        return String.valueOf(value);
      for (SoftLinkTranslation translation : getSoftLinkTranslations()) {

        Pattern pattern = translation.getPattern();
        Matcher matcher = pattern.matcher(key);
        if (matcher.matches()) {
          link = matcher.replaceAll(translation.getReplacePattern());
          break;
        }
      }
    }
    return link;
  }

  protected void registerBookMarkUsage(BookmarkUsage usage) {
    bookmarkUsages.add(usage);
  }

  /**
   * Make the specified absolute path relative to the root folder (the primary document)
   *
   * @param pathSpec
   * @return
   * @throws IOException
   */
  protected String resolveLibraryPath(String pathSpec) throws IOException {

    String lib = getLibrary();

    // Every path must be made relative to the root (document). The pathSpec is however relative to the currentFolder
    // Since the currentFolder is an absolute path that is always in the library (this is a requirement) we can create a relative path from it
    // (just cut of the absolute part that is the library location)
    // So the first thing we will do is create a relative path from the currentFolder to the library itself.

    String stepBack = stepBack(getRootFolder(), lib);

    String actualLocation = stepBack + pathSpec.substring(1); // Strip the '/' because was absolute
    return actualLocation;
  }

  /**
   * Create a 'step back path' from the specified folder to the destinationFolder (the root of the structure)
   *
   * @param currentFolder
   * @param destinationFolder (must be closer to the root than currentFolder)
   * @return
   * @throws IOException
   */
  protected String stepBack(String currentFolder, String destinationFolder) throws IOException {
    String destination = ThothUtil.stripPrefix(canonicalize(destinationFolder), "/");
    String current = ThothUtil.stripPrefix(canonicalize(currentFolder), "/");

    String[] destinationPath = destination.split("/");
    String[] currentPath = current.split("/");

    int sharedIdx = 0;
    for (int i = 0; i < destinationPath.length && i < currentPath.length; i++) {
      if (destinationPath[i].equals(currentPath[i]))
        sharedIdx++;
    }

    int stepBackCount = currentPath.length - sharedIdx;

    String actualLocation = "";
    for (int i = 0; i < stepBackCount; i++)
      actualLocation += "../";
    return actualLocation;
  }

  /**
   * Creates a bookmark for a line that contains a header (starts with a #) The headerIndent specifies the number of (additional) indents to create for the
   * header. This might come in handy when including other documents that have headers of the wrong level (including level 1 headers can now put them at a
   * higher level)
   *
   * @param line
   * @param headerIndent
   * @return
   */
  protected String createBookmark(String line, int headerIndent) {

    line = adjustHeaderLevel(line, headerIndent);

    int level = 0;
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) == '#')
        level++;
      else
        break;
    }
    updateHeaderCounters(level);

    if (level <= maxNumberingLevel) {
      String prefix = getHeaderCounter(level);
      line = line.substring(0, level) + prefix + line.substring(level);
    }

    String title = line.substring(level).trim();
    String id = ThothUtil.encodeBookmark(title, true);

    // Make sure we do not get into trouble with HTML tags in the title
    title = title.replaceAll("\\<", "\\\\<");
    title = title.replaceAll("\\>", "\\\\>");

    if (isValidBookmark(id)) {
      bookmarks.add(new Bookmark(level, id, title));
    }
    return "\n" + line + "\n";
  }

  public List<Bookmark> getBookmarks() {
    return bookmarks;
  }

  protected String adjustHeaderLevel(String line, int headerIndent) {
    if (headerIndent >= 0) {
      for (int i = 0; i < headerIndent; i++)
        line = "#" + line;
    } else {
      for (int i = headerIndent; i < 0; i++)
        if (line.startsWith("##"))
          line = line.substring(1);
    }
    return line;
  }

  /**
   * Increments the header counter of the current level; and resets all counters with a higher number
   *
   * @param level
   */
  protected void updateHeaderCounters(int level) {
    if (level < headerCounters.length) {
      headerCounters[level]++;
      for (int idx = level + 1; idx < headerCounters.length; idx++)
        headerCounters[idx] = 0;
    }
  }

  /**
   * Returns the numbering prefix that is used for all headers
   *
   * @param level
   * @return
   */
  protected String getHeaderCounter(int level) {
    String result = "";
    for (int i = 1; i <= level; i++)
      result += String.valueOf(headerCounters[i]) + ".";
    if (result.endsWith("."))
      result = result.substring(0, result.length() - 1);
    return result + " ";
  }

  /**
   * Numbering of headers will only happen upto the specified level
   *
   * @param maxNumberingLevel
   */
  public void setMaxNumberingLevel(int maxNumberingLevel) {
    this.maxNumberingLevel = maxNumberingLevel;
  }

  /**
   * Returns true if the id is a valid bookmark/identifier.
   *
   * @param id
   * @return
   */
  protected boolean isValidBookmark(String id) {
    return id.trim().length() != 0 && Character.isJavaIdentifierPart(id.charAt(0));
  }

  protected Integer getHeaderLevel(String[] args, String originalSpecification) {
    Integer headerLevel = null;
    try {
      if (args.length > 1)
        headerLevel = Integer.parseInt(args[1].trim());
    } catch (NumberFormatException e) {
      error("Header level is not numeric: " + originalSpecification);
    }
    return headerLevel;
  }

  /**
   * Returns just the folder part of a file spec. The result will always end with a '/'
   *
   * @param filespec
   * @return
   */
  protected String getFolderName(String filespec) {
    String folderName = ThothUtil.normalSlashes(filespec);
    int idx = folderName.lastIndexOf("/");
    if (idx == -1)
      return "";
    if (idx != -1)
      folderName = folderName.substring(0, idx);
    if (!folderName.endsWith("/"))
      folderName += "/";
    return folderName;
  }

  /**
   * Adds a Markdown comment to the output stream
   *
   * @param out
   * @param message
   */
  protected void comment(PrintStream out, String message) {
    if (addComments)
      out.println("\n[//]: # \"" + message + "\"");
  }

  /**
   * Generates the Table Of Contents in Markdown format based on the Bookmarks known to this FileProcessor
   *
   * @param document
   * @return
   */
  protected String createToc(String document) {
    StringBuilder toc = new StringBuilder();

    int minimumLevel = 99;
    for (Bookmark bookmark : bookmarks)
      minimumLevel = Math.min(minimumLevel, bookmark.getLevel());

    boolean first = true;
    for (Bookmark bookmark : bookmarks) {
      if (first)
        toc.append("<" + TABLEOFCONTENTS_TAG + ">");
      first = false;
      if (bookmark.getLevel() == minimumLevel)
        toc.append("\n");
      String tocLine = String.format("[%s](#%s)", bookmark.getTitle(), bookmark.getId());
      if (bookmark.getLevel() > minimumLevel) {
        tocLine = "- " + tocLine;
        for (int i = minimumLevel; i < bookmark.getLevel(); i++)
          tocLine = "  " + tocLine;
      } else
        tocLine = "###" + tocLine.trim();

      toc.append(tocLine + "\n");
      if (bookmark.getLevel() == minimumLevel)
        toc.append("\n");
    }
    if (!bookmarks.isEmpty())
      toc.append("</" + TABLEOFCONTENTS_TAG + ">");

    return document.replace("\n\\tableofcontents", toc.toString().trim());
  }

  /**
   * Will translate Windows file specs into a Java/Linux file spec by translating back slashes into forward slashes. Will also make sure the path ends with a
   * '/'
   *
   * @param path
   * @return
   */
  protected String fixFolderSpec(String path) {
    if (path != null) {
      path = path.replaceAll("\\\\", "/");
      path = path.replaceAll("//", "/");
      if (path.length() != 0)
        path = ThothUtil.suffix(path, "/");
    }
    return path;
  }

  /**
   * Sets the library path that serves as the root for the entire documentation structure
   *
   * @param library
   * @throws IOException
   */
  public void setLibrary(String librarySpec) throws IOException {
    String library = fixFolderSpec(createFileHandle(librarySpec).getCanonicalPath());
    this.library = library;
  }

  /**
   * Returns the library path that serves as the root for the entire documentation structure
   *
   * @param library
   */
  public String getLibrary() {
    if (library == null)
      library = determineLibraryFallBack();
    return library;
  }

  /**
   * This tries to find the library root based on the fact that the library root contains a file called 'softlinks.properties' If that file cannot be found then
   * the folder of the root document is taken as a last resort.
   * 
   * @return
   */
  protected String determineLibraryFallBack() {
    String currentFolder = ThothUtil.normalSlashes(getRootFolder());
    FileHandle file = createFileHandle(currentFolder);
    String lookFor = getSoftLinkFileName();
    if (lookFor == null)
      lookFor = SOFTLINKS_PROPERTIES_DFLT;

    boolean found = false;
    if (lookFor != null) {
      while (!found && file != null && file.isDirectory()) {
        String[] list = file.list();
        if (list != null)
          for (String name : list) {
            if (lookFor.equals(name)) {
              found = true;
              break;
            }
          }
        if (!found)
          file = file.getParentFile();
      }
    }
    if (found)
      return file.getAbsolutePath();
    return getRootFolder();
  }

  /**
   * Creates a map of key/value pairs based on an array of arguments (-key [value] format, value is optional)
   *
   * @param args
   * @return
   */
  protected static Map<String, String> getArgumentsMap(String[] args) {
    Map<String, String> arguments = new HashMap<String, String>();

    for (int i = 0; i < args.length; i++) {
      String key = args[i];
      if (!key.startsWith("-"))
        throw new IllegalArgumentException("Missing parameter prefix for value " + key);
      String value = i + 1 < args.length ? args[i + 1] : null;

      if (value != null && value.startsWith("-")) {
        value = null; // A parameter without a value (a flag) detected
      } else
        i++; // Make sure we do not interpret the value as a key now
      arguments.put(key.toLowerCase().trim().substring(1), value);
    }
    return arguments;
  }

  /**
   * Sets the current line number
   *
   * @param line
   */
  protected void setLineNumber(int line) {
    getCurrentLineInfo().setLine(line);
  }

  /**
   * Retrieves information about the current line in the form of a LineInfo
   *
   * @return
   */
  protected LineInfo getCurrentLineInfo() {
    if (currentInfo.isEmpty())
      return null;
    else
      return currentInfo.peek();
  }

  /**
   * Notifies that a new file is about to be processed; using a stack mechanism. When the file is processed notify that with {@link #endFile()}
   *
   * @param fileName
   * @throws IOException
   */
  protected void startNewFile(String fileName) throws IOException {
    if (getLibrary().length() != 0 && fileName.startsWith(getLibrary()))
      fileName = fileName.substring(getLibrary().length() - 1);
    currentInfo.push(new LineInfo(ThothUtil.stripPrefix(fileName, "/"), 0));
  }

  /**
   * Notifies the end of processing the current file; by popping it of the stack
   */
  protected void endFile() {
    currentInfo.pop();
  }

  /**
   * Will remove any relative path parts from a file spec
   *
   * @param fileName
   * @return
   */
  protected String canonicalize(String fileName) {
    if (STDIN.equalsIgnoreCase(fileName))
      return STDIN;

    FileHandle fileHandle = createFileHandle(fileName);
    return fixFolderSpec(fileHandle.getCanonicalPath());
  }

  /**
   * Will load the file that contains the soft links specs in the form of either from=to or fromspec=tospec The fromspec can use a '*' as a wildcard; which can
   * be used as a back reference in the tospec. For example ~*=/my/path/$1/class.md will translate ~Order to /my/path/Order/class.md
   *
   * @throws IOException
   */
  protected void loadSoftLinks() throws IOException {
    softLinkMappings = new HashMap<String, String>();
    softLinkTranslations = new ArrayList<SoftLinkTranslation>();
    String softLinkFileName = getSoftLinkFileName();

    // If not set then try the default; but do not warn if that file is not found
    boolean showErrorNoNotFound = true;
    if (softLinkFileName == null) {
      softLinkFileName = SOFTLINKS_PROPERTIES_DFLT;
      showErrorNoNotFound = false;
    }

    if (softLinkFileName.startsWith("/"))
      softLinkFileName = softLinkFileName.substring(1);
    String fileName = getLibrary() + softLinkFileName;

    FileHandle file = createFileHandle(fileName);
    if (file.isFile()) {
      loadSoftLinks(file.getInputStream());
    } else if (showErrorNoNotFound)
      error("Soft link file " + softLinkFileName + " not found at " + file.getAbsolutePath());
  }

  protected void loadSoftLinks(InputStream is) throws UnsupportedEncodingException, IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    String line = br.readLine();
    while (line != null) {
      // Skip comments
      if (!line.trim().startsWith("#")) {
        int idx = line.indexOf('=');
        if (idx != -1) {
          String key = line.substring(0, idx).trim();
          String value = line.substring(idx + 1).trim();
          if (key.indexOf('*') != -1) {
            softLinkTranslations.add(new SoftLinkTranslation(key, value));
          } else {
            if (value.startsWith("/"))
              value = value.replaceAll(" ", "%20");
            softLinkMappings.put(key.toLowerCase(), value);
          }
        }
      }
      line = br.readLine();
    }
    br.close();
  }

  public Map<String, String> getSoftLinkMappings() {
    return softLinkMappings;
  }

  public List<SoftLinkTranslation> getSoftLinkTranslations() {
    return softLinkTranslations;
  }

  /**
   * Will validate the bookmarks specifications and add errors for any bookmarks that are invalid
   */
  protected void validate() {
    Set<String> validBookmarks = new HashSet<String>();

    for (Bookmark bookmark : this.bookmarks) {
      validBookmarks.add(bookmark.getId());
      // Also add a bookmark without a numeric prefix; a manually entered bookmark could be without a numeric prefix
      // (Just used the title without the number)
      validBookmarks.add(ThothUtil.stripNumericPrefix(bookmark.getId()));
    }
    for (BookmarkUsage usage : this.bookmarkUsages) {
      if (!validBookmarks.contains(usage.getBookmark())) {
        error("Invalid bookmark: #" + usage.getBookmark());
      }
    }
  }

  public List<BookmarkUsage> getBookmarkUsages() {
    return bookmarkUsages;
  }

  public Map<String, String> getMetaTags() {
    return metaTags;
  }

  /**
   * Returns the list of error messages
   *
   * @return
   */
  public List<ProcessorError> getErrors() {
    return errors;
  }

  public boolean hasErrors() {
    return !this.errors.isEmpty();
  }

  public void setRootFolder(String rootFolder) {
    this.rootFolder = canonicalize(rootFolder);
  }

  public void setSoftlinkFile(String softLinkFileName) {
    this.softLinkFileName = softLinkFileName;
  }

  public String getSoftLinkFileName() {
    return softLinkFileName;
  }

  public boolean containsToc() {
    return this.containsToc;
  }

  public String getRootFolder() {
    return rootFolder == null ? "./" : rootFolder;
  }

  public void setAddComments(boolean addComments) {
    this.addComments = addComments;
  }

  public void setStripTrailingWhitespace(boolean stripTrailingWhitespace) {
    this.stripTrailingWhitespace = stripTrailingWhitespace;
  }

  protected String makeRelativeToLibrary(FileHandle fileHandle) throws IOException {
    String canonicalFile = ThothUtil.normalSlashes(fileHandle.getCanonicalPath());
    String library = getLibrary();
    String result;
    if (!canonicalFile.startsWith(library)) {
      error("Path " + canonicalFile + " is not located in library " + library);
      result = fileHandle.getAbsolutePath();
    } else {
      result = canonicalFile.substring(library.length());
    }
    return result;
  }

  protected boolean inCodeBlock(String line) {
    return line.startsWith("\t") || line.startsWith("    ");
  }

  protected int asInt(String intSpec) {
    try {
      return Integer.parseInt(intSpec);
    } catch (Exception x) {
      error("Invalid value for integer specified " + intSpec);
    }
    return 0;
  }

  public void setOut(PrintStream out) {
    this.out = out;
  }

  public PrintStream getOut() {
    return out;
  }

  /**
   * Abbreviation for System.out.println()
   */
  protected void pl() {
    out.println();
  }

  /**
   * Abbreviation for System.out.println(String string)
   */
  protected void pl(String string) {
    out.println(string);
  }

  public FileHandle createFileHandle(String filename) {
    return fileSystem.getFileHandle(filename);
  }
}
