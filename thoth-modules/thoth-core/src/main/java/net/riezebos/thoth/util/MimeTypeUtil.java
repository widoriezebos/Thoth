package net.riezebos.thoth.util;

import java.io.IOException;
import java.util.Properties;

public class MimeTypeUtil {
  private static final String MAPFILE = "net/riezebos/thoth/util/mimetypes.properties";
  private static Properties types = null;

  public static String getMimeType(String extension) {
    if (extension == null)
      return null;

    if (types == null) {
      try {
        Properties props = new Properties();
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(MAPFILE));
        types = props;
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
    return (String) types.get(extension.toLowerCase());
  }
}
