package net.riezebos.thoth.markdown.filehandle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.riezebos.thoth.util.ThothUtil;

public class ZipFileSystem extends ClasspathFileSystem {

  private FileHandle zipFile;
  private String zipRootEntry;

  public ZipFileSystem(FileHandle zipFile) throws IOException {
    this(zipFile, null);
  }

  public ZipFileSystem(FileHandle zipFile, String root) throws IOException {
    setup(zipFile, root);
  }

  public ZipFileSystem(String pathToZipFile) throws IOException {

    String basePath = ThothUtil.getPartAfterFirst(pathToZipFile, "!");
    String zipFileName = ThothUtil.getPartBeforeFirst(pathToZipFile, "!");
    FileHandle check = new BasicFileSystem().getFileHandle(zipFileName);

    setup(check, basePath);
  }

  public long getLatestModification() {
    return zipFile.lastModified();
  }

  protected void setup(FileHandle fileHandle, String basePath) throws IOException {
    if (basePath == null)
      basePath = "/";
    basePath = ThothUtil.prefix(basePath, "/");
    basePath = ThothUtil.suffix(basePath, "/");
    zipRootEntry = basePath;

    if (!fileHandle.isFile())
      throw new FileNotFoundException("Zip file not found: " + fileHandle.getAbsolutePath());
    zipFile = fileHandle;

    setFileSystemRoot("");
    scanZip(basePath);
  }

  protected void scanZip(String relativeInZip2) throws IOException {

    ZipInputStream zip = new ZipInputStream(zipFile.getInputStream());
    ZipEntry ze = null;

    while ((ze = zip.getNextEntry()) != null) {
      String entryName = ThothUtil.prefix(ze.getName(), "/");
      if (entryName.startsWith(zipRootEntry)) {
        entryName = ThothUtil.prefix(entryName.substring(zipRootEntry.length()), "/");
        if (ze.isDirectory())
          registerFolder(entryName, ze.getTime());
        else
          registerFile(entryName, ze.getTime(), ze.getSize());
      }
    }
    zip.close();
  }

  public boolean isFile(FileHandle fileHandle) {
    if (fileHandle == null || fileHandle.getAbsolutePath() == null || fileHandle.getAbsolutePath().length() == 0)
      return false;
    String canonicalPath = fileHandle.getCanonicalPath();
    if (getFolders().contains(canonicalPath))
      return false;

    String folderName = ThothUtil.prefix(ThothUtil.getFolder(canonicalPath), "/");
    List<String> folderContents = getFolderContents(folderName);
    return folderContents != null && folderContents.contains(ThothUtil.getFileName(canonicalPath));
  }

  public InputStream getInputStream(FileHandle fileHandle, boolean throwOnError) throws IOException {

    ZipInputStream zip = new ZipInputStream(zipFile.getInputStream());
    ZipEntry ze = null;

    String match = ThothUtil.suffix(zipRootEntry, "/") + ThothUtil.stripPrefix(fileHandle.getCanonicalPath(), "/");
    while ((ze = zip.getNextEntry()) != null) {
      String entryName = ThothUtil.prefix(ze.getName(), "/");
      if (match.equals(entryName))
        return zip;
    }
    zip.close();
    if (throwOnError)
      throw new FileNotFoundException(match);
    return null;
  }

}
