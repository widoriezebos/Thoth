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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.riezebos.thoth.beans.BookmarkUsage;
import net.riezebos.thoth.markdown.util.DocumentNode;
import net.riezebos.thoth.markdown.util.ProcessorError;
import net.riezebos.thoth.util.ThothUtil;

/**
 * This class is built to be compatible with JDK1.6 (See project settings for the compiler) so that users will not run into trouble with an older JRE installed
 *
 * @author wido
 */
public class IncludeProcessor extends FileProcessor {

  private int MAX_INCLUDE_DEPTH = 10;
  private Pattern includeMarked = Pattern.compile("\\<\\<\\[(.*?)\\]"); // Handle Marked style includes i.e. '<<[MyDocument.md]>>'
  private Pattern includeLatex = Pattern.compile("\\\\include\\{(.*?)\\}"); // Handle Latex style includes i.e. '\include{MyDocument.md}'
  private Pattern includeImages = Pattern.compile("\\\\includeimages\\{(.*?)\\}"); // Handle image icludes based on a filespec
  private Pattern hyperlink = Pattern.compile("\\[(.*?)\\]\\(([^\")]*)(.*?)\\)");
  private DocumentNode documentStructure;

  public String execute(String fileName, InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(50000);
    PrintStream result = new PrintStream(bos, false, "UTF-8");
    reset();
    String path = fileName == null ? STDIN : fileName;
    startNewFile(path);
    Stack<DocumentNode> includeStack = new Stack<DocumentNode>();
    documentStructure = new DocumentNode(path, ThothUtil.getNameOnly(fileName), 0, 0);
    includeStack.push(documentStructure);
    processFile(getRootFolder(), in, result, includeStack, 0);
    result.flush();

    validate();

    if (containsToc()) {
      String document = new String(bos.toByteArray(), "UTF-8");
      return createToc(document);
    } else
      return bos.toString("UTF-8");
  }

  protected void processFile(String currentFolder, InputStream fis, PrintStream out, Stack<DocumentNode> includeStack, int headerIndent) throws IOException {
    if (includeStack.size() > MAX_INCLUDE_DEPTH) {
      String errorMessage = "\n\nMaximum include depth of " + MAX_INCLUDE_DEPTH + " reached. Stopping here.\nStack:\n";
      for (DocumentNode documentNode : includeStack) {
        errorMessage += "\t" + documentNode.getPath() + "\n";
        out.println(errorMessage);
        error(errorMessage.trim());
      }
    } else {
      BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
      int lineNumber = 1;
      String line = readLine(br);
      while (line != null) {
        setLineNumber(lineNumber++);
        updateTocFlag(line);
        // Handle include Marked style
        Matcher matcher = includeMarked.matcher(line);
        boolean found = matcher.find();
        if (!found) {
          matcher = includeLatex.matcher(line);
          found = matcher.find();
        }
        if (found) {
          String prefix = line.substring(0, matcher.start());
          String suffix = line.substring(matcher.end());
          out.print(prefix);
          String fileName = matcher.group(1);
          String indentStr = "0";

          int idx = fileName.lastIndexOf(',');
          if (idx != -1) {
            indentStr = fileName.substring(idx + 1).trim();
            fileName = fileName.substring(0, idx);

          }
          int headerIncrement = asInt(indentStr);

          include(currentFolder, fileName, out, includeStack, headerIndent + headerIncrement);
          out.print(suffix);
        } else {
          // Handle bookmarks
          if (line.startsWith("#"))
            line = createBookmark(line, headerIndent);

          Matcher imageMatcher = includeImages.matcher(line);
          if (imageMatcher.find()) {
            String prefix = line.substring(0, imageMatcher.start());
            String suffix = line.substring(imageMatcher.end());
            out.print(prefix);
            String fileName = imageMatcher.group(1);
            includeImages(currentFolder, fileName, out, includeStack, headerIndent);
            out.print(suffix);
          } else {
            line = handleLinks(currentFolder, line, includeStack);
            extractMetaInfo(line);
            out.println(line);
          }
        }
        line = readLine(br);
      }
      br.close();
    }
  }

  protected int asInt(String indentStr) {
    try {
      return Integer.parseInt(indentStr);
    } catch (Exception x) {
      error("Invalid value for integer specified " + indentStr);
    }
    return 0;
  }

  protected String handleLinks(String currentFolder, String line, Stack<DocumentNode> includeStack) throws IOException {
    // Small optimization to avoid RegExp stuff for lines that do not need processing anyways
    if (line.indexOf("[") != -1) {
      // Check whether we need to fix relative paths for images
      Matcher matcher = hyperlink.matcher(line);
      int idx = 0;
      while (matcher.find(idx)) {
        int start = matcher.start();
        boolean embed = (start > 0 && line.charAt(start - 1) == '!');

        int end = matcher.end();
        String pathSpec = matcher.group(2).trim();
        String afterPath = matcher.group(3);
        if (afterPath != null && afterPath.trim().length() == 0)
          afterPath = null;

        idx = matcher.end(); // Make sure we do not loop

        if (pathSpec.indexOf("://") == -1) {
          // Fix absolute paths

          // Process soft links here
          pathSpec = translateSoftLink(pathSpec);
          String description = matcher.group(1);
          if (pathSpec.startsWith("/")) {

            String actualLocation = resolveLibraryPath(pathSpec);

            if (embed)
              createDocumentNode(pathSpec, description, includeStack);

            String newLink = "[" + description + "](" + actualLocation + ")";
            line = line.substring(0, start) + newLink + line.substring(end);
            idx = start + newLink.length();
            matcher = hyperlink.matcher(line);

          } else
          // fix relative links
          {
            // First determine the relative path from the root to where we are now; which actually is the currentFolder without the library prefix
            String relativePart = currentFolder.substring(getRootFolder().length());
            if (relativePart.startsWith("/"))
              relativePart = relativePart.substring(1);
            String actualLocation = relativePart + pathSpec;
            actualLocation = actualLocation.replace(" ", "%20"); // Encode spaces because that will also break an image link

            String pathname = getRootFolder() + actualLocation;
            if (pathSpec.startsWith("#")) {
              String bookmark = pathSpec.substring(1);
              BookmarkUsage usage = new BookmarkUsage();
              usage.setCurrentLineInfo(getCurrentLineInfo());
              usage.setBookMark(bookmark);
              registerBookMarkUsage(usage);
            } else {
              // Do not check http:// or mailto: kind of links
              if (pathname.indexOf(':') == -1) {
                File check = new File(pathname.replaceAll("%20", " "));
                if (!check.exists())
                  error(getCurrentLineInfo() + ": Link invalid: " + pathSpec);
              }
            }

            if (embed)
              createDocumentNode(makeRelativeToLibrary(new File(pathname)), description, includeStack);

            String newLink = "[" + description + "](" + actualLocation + (afterPath != null ? " " + afterPath : "") + ")";
            line = line.substring(0, start) + newLink + line.substring(end);
            idx = start + newLink.length();
            matcher = hyperlink.matcher(line);
          }
        }
      }
    }
    return line;
  }

  protected void createDocumentNode(String actualLocation, String description, Stack<DocumentNode> includeStack) {
    // Do not create document nodes for local bookmarks
    if (!actualLocation.startsWith("#") && actualLocation.indexOf("://") == -1) {
      DocumentNode documentNode = new DocumentNode(actualLocation, description, getCurrentLineInfo().getLine(), includeStack.size());
      includeStack.peek().addChild(documentNode);
    }
  }

  protected void includeImages(String currentFolder, String includeSpec, PrintStream out, Stack<DocumentNode> includeStack, int headerIndent)
      throws IOException {
    String args[] = includeSpec.split(",");
    Integer headerLevel = getHeaderLevel(args, includeSpec);

    String fileName = translateSoftLink(args[0]);

    if (fileName.startsWith("/"))
      fileName = resolveLibraryPath(fileName);

    String folderName = getFolderName(fileName);
    String spec = ThothUtil.getFileName(args[0]);

    String absoluteFolder = currentFolder + folderName;
    File folder = new File(absoluteFolder);
    Pattern pattern = ThothUtil.specAsRegExp(spec);

    List<String> result = new ArrayList<String>();
    File[] listFiles = folder.listFiles();
    if (listFiles == null) {
      error("Image include spec invalid: " + includeSpec);
    } else {
      for (File file : listFiles) {
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.matches())
          result.add(folderName + file.getName());
      }
    }
    Collections.sort(result);

    comment(out, "includeimages " + includeSpec);

    for (String imagePath : result) {
      if (headerLevel != null && headerLevel > 0) {
        out.println();
        String line = "";
        for (int i = 0; i < headerLevel; i++)
          line += "#";
        line += ThothUtil.getNameOnly(imagePath);
        line = createBookmark(line, headerIndent);
        out.println(line + "\n");
      }
      out.println("![](" + imagePath.replaceAll(" ", "%20") + ")");
    }
  }

  protected void include(String currentFolder, String fileToInclude, PrintStream out, Stack<DocumentNode> includeStack, int headerIndent)
      throws FileNotFoundException, IOException {

    String softTranslated = translateSoftLink(fileToInclude);
    String fileName = softTranslated;
    String pathname;

    if (fileName.startsWith("/")) {
      fileName = resolveLibraryPath(fileName);
      pathname = getRootFolder() + fileName;
    } else
      pathname = currentFolder + fileName;

    if (fileName.startsWith("/"))
      pathname = fileName;
    File file = new File(pathname.replaceAll("%20", " "));
    if (!file.exists()) {
      String errorMessage = "Include not found: " + fileToInclude;
      if (!fileToInclude.equals(softTranslated))
        errorMessage += " (translated by softlink to '" + softTranslated + "')";
      error(errorMessage.trim());
    } else {
      String newFolder = pathname;
      int lastIdx = newFolder.lastIndexOf("/");
      if (lastIdx != -1)
        newFolder = newFolder.substring(0, lastIdx + 1);

      DocumentNode includeUsage =
          new DocumentNode(makeRelativeToLibrary(file), ThothUtil.getNameOnly(fileToInclude), getCurrentLineInfo().getLine(), includeStack.size());
      includeStack.peek().addChild(includeUsage);
      includeStack.push(includeUsage);
      startNewFile(pathname);
      comment(out, "Include " + fileToInclude);
      processFile(newFolder, new FileInputStream(file), out, includeStack, headerIndent);
      endFile();
      includeStack.pop();
    }
  }

  public DocumentNode getDocumentStructure() {
    return documentStructure;
  }

  protected static void printUsage() {
    pl("Usage: java " + IncludeProcessor.class.getName() + " <arguments>");
    pl();
    pl("Description:");
    pl("  Will process 'includes' and fix image paths for included files as well");
    pl("  Supports the notion of a Library (which serves as the root for all documents)");
    pl("  When specifiying an absolute path; it will be made relative to the library");
    pl();
    pl("Arguments:");
    pl("  -file <filename>");
    pl("      Optional; specifies the input file. When not given STDIN is used");
    pl("  -target <filename>");
    pl("      Optional; specifies the output file. When not given STDOUT is used");
    pl("  -origin <origin>");
    pl("       Optional; Base directory of input. Used for determining relative paths");
    pl("       When not specified env variable MARKED_ORIGIN is used (required then)");
    pl("  -library <library>");
    pl("       The location of the documentation library. Absolute paths are");
    pl("       relative to the Library");
    pl("  -softlinkfile <filename>");
    pl("       The location of the soft links property file. Path is relative to the Library.");
    pl("       Default is 'softlinks.properties'");
    pl("  -numbering <level>");
    pl("       Auto number headings up to the specified level. Default is " + DEFAULT_NUMBERING_LEVEL);
    pl("       Set to 0 to disable");
    pl("  -noerrors");
    pl("       Suppresses adding any error report at the end of the document");
    pl("  -nocomments");
    pl("       Suppresses adding a comment for every processed include. Default is false");
    pl("  -nostrip");
    pl("       Suppresses stripping trailing whitespace. Default is false");
    pl();
    pl("Supported keywords in input:");
    pl("  \\include{Filename.md}");
    pl("       Includes the file Filename.md. Filename.md can be an absolute");
    pl("       path (will be relative to the Library) or a relative path");
    pl("       (relative to the file that does the include) Note that nested");
    pl("       includes are supported");
    pl("  \\tableofcontents");
    pl("       Will generate a table of contents based on the #section headers in");
    pl("       the (fully) generated input");
  }

  public static void main(String[] args) throws IOException {

    Map<String, String> arguments = getArgumentsMap(args);
    boolean help = arguments.containsKey("help");
    boolean nocomments = arguments.containsKey("nocomments");
    boolean nostrip = arguments.containsKey("nostrip");
    boolean noErrors = arguments.containsKey("noerrors");

    help |= arguments.containsKey("h");
    if (help) {
      printUsage();
      System.exit(0);
    }

    int numberingLevel = DEFAULT_NUMBERING_LEVEL;

    String numberLevelStr = arguments.get("numbering");
    if (numberLevelStr != null)
      numberingLevel = Integer.parseInt(numberLevelStr);

    Map<String, String> env = System.getenv();
    String rootFolder = env.get("MARKED_ORIGIN");
    InputStream in = System.in;

    String fileName = arguments.get("file");
    if (fileName != null) {
      in = new FileInputStream(fileName);
      int idx = fileName.lastIndexOf("/");
      if (idx != -1)
        rootFolder = fileName.substring(0, idx);
    }

    if (rootFolder == null)
      rootFolder = arguments.get("origin");
    if (rootFolder == null)
      throw new IllegalArgumentException("No origin set. Either use the -origin parameter or the MARKED_ORIGIN environment variable");

    String library = arguments.get("library");
    String softlinkFile = arguments.get("softlinkfile");

    IncludeProcessor processor = new IncludeProcessor();
    rootFolder = processor.fixFolderSpec(rootFolder);
    processor.setLibrary(library);
    processor.setRootFolder(rootFolder);
    processor.setMaxNumberingLevel(numberingLevel);
    processor.setAddComments(!nocomments);
    processor.setStripTrailingWhitespace(!nostrip);
    if (softlinkFile != null)
      processor.setSoftlinkFile(softlinkFile);
    String result = processor.execute(fileName, in);
    List<ProcessorError> errors = processor.getErrors();
    if (!errors.isEmpty() && !noErrors) {
      result += "\n\tThe following problems occurred during generation of this document:\n";
      for (ProcessorError error : errors)
        result += "\t" + (error.getErrorMessage().replaceAll("\n", "\n\t").trim()) + "\n";
    }

    String target = arguments.get("target");
    if (target != null) {
      FileOutputStream fos = new FileOutputStream(target);
      fos.write(result.getBytes("UTF-8"));
      fos.close();
      System.out.println("Written to " + target);
    } else
      System.out.println(result);
  }

}
