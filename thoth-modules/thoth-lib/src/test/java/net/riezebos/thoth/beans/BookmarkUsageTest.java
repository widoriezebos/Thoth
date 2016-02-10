package net.riezebos.thoth.beans;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.riezebos.thoth.markdown.util.LineInfo;

public class BookmarkUsageTest {

  @Test
  public void test() {
    BookmarkUsage usage = new BookmarkUsage();
    usage.setBookMark("mark1");
    usage.setCurrentLineInfo(new LineInfo("file", 1));
    assertEquals(1, usage.getCurrentLineInfo().getLine());
    assertEquals("/file", usage.getCurrentLineInfo().getFile());
    assertEquals("/file(1): #mark1", usage.toString());
    
    BookmarkUsage usage2 = new BookmarkUsage("mark2");
    assertEquals("mark2", usage2.getBookmark());
  }

}
