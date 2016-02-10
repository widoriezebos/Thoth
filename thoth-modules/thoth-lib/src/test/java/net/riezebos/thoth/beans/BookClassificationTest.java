package net.riezebos.thoth.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class BookClassificationTest {

  @Test
  public void test() {
    BookClassification bookClassification = new BookClassification("class1");

    List<Book> books = new ArrayList<Book>();
    Book bookB = new Book("b", "path1");
    Book bookA = new Book("a", "path2");
    books.add(bookB);
    books.add(bookA);

    BookClassification bookClassification2 = new BookClassification("class2", books);
    bookClassification2.sortBooks();

    assertEquals("class1", bookClassification.getName());
    assertEquals("class1", bookClassification.toString());
    assertEquals(bookA, bookClassification2.getBooks().get(0));

    assertTrue(bookClassification.compareTo(bookClassification2) < 1);
  }

}
