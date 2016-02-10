package net.riezebos.thoth.beans;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BookmarkTest {

  @Test
  public void test() {
    Bookmark bookmark = new Bookmark(1, "one", "Title one");
    assertEquals(1, bookmark.getLevel());
    assertEquals("one", bookmark.getId());
    assertEquals("Title one", bookmark.getTitle());
    assertEquals("Title one", bookmark.toString());
  }

}
