package net.riezebos.thoth.beans;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class BookTest {

  @Test
  public void test() {
    Book book = new Book("name", "/some/path/name.md");
    Book book2 = new Book("name.txt", "name.md");
    Book book3 = new Book("SomeTitle.md", "/some/path/name.md");

    Map<String, String> meta = new HashMap<String, String>();
    meta.put("a", "1");
    meta.put("b", "2");
    meta.put("c", "3");
    book.setMetaTags(meta);

    assertEquals("name", book.getName());
    assertEquals("/some/path", book.getFolder());
    assertEquals("name.txt", book2.getName());
    assertEquals("/some/path/name.md", book.getPath());
    assertEquals("name.md", book2.getPath());
    assertEquals("SomeTitle", book3.getTitle());

    assertTrue(book.compareTo(book2) < 1);

    assertEquals("1", book.getMetaTag("a"));
    assertArrayEquals(new String[] {"a", "b", "c"}, book.getMetaTagKeys().toArray(new String[0]));

    assertEquals(book.toString(), book.getPath());
  }

}
