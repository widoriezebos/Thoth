package net.riezebos.thoth.util;

public class LibraryTestBase {

  protected boolean stringsEqual(String str1, String str2) {
    if (str1 == null && str2 == null)
      return true;
    if (str1 == null || str2 == null)
      return false;
    str1 = str1.replaceAll("\r", "");
    str2 = str2.replaceAll("\r", "");
    return str1.equals(str2);
  }

}
