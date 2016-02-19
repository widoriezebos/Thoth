package net.riezebos.thoth.markdown.filehandle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileHandleUtil {
  private static final int INITIAL_CHUNK_SIZE = 63 * 1024;

  public static void copy(InputStream source, OutputStream target) throws IOException {
    int chunkSize = INITIAL_CHUNK_SIZE;
    byte[] ba = new byte[chunkSize];

    while (true) {

      int bytesRead = readBlocking(source, ba, 0, chunkSize);
      if (bytesRead > 0) {
        target.write(ba, 0, bytesRead);
      } else {
        break; // EOF
      }
    }
  }

  public static final int readBlocking(InputStream in, byte b[], long off, int len) throws IOException {
    int totalBytesRead = 0;

    if (in != null) {
      while (totalBytesRead < len) {
        int bytesRead = in.read(b, (int) (off + totalBytesRead), len - totalBytesRead);
        if (bytesRead < 0) {
          break;
        }
        totalBytesRead += bytesRead;
      }
    }
    return totalBytesRead;
  }

  public static void cleanupCreatedFiles(FileSystem targetFileSystem) {
    for (String fileName : targetFileSystem.getCreatedFiles())
      targetFileSystem.getFileHandle(fileName).delete();
    Set<String> createdPaths = new HashSet<String>();

    for (String fileName : targetFileSystem.getCreatedFiles()) {
      FileHandle folder = targetFileSystem.getFileHandle(fileName).getParentFile();
      do {
        String absolutePath = folder.getAbsolutePath();
        if (absolutePath.length() != 0 && !"/".equals(absolutePath))
          createdPaths.add(absolutePath);
        folder = folder.getParentFile();
      } while (folder != null);
    }

    List<String> tobeDeleted = new ArrayList<String>(createdPaths);
    Collections.sort(tobeDeleted, new Comparator<String>() {
      @Override
      public int compare(String str1, String str2) {
        return str2.length() - str1.length();
      }
    });

    for (String path : tobeDeleted)
      targetFileSystem.getFileHandle(path).delete();

  }

}
