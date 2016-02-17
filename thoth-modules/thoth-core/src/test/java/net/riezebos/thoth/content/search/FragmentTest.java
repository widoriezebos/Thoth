package net.riezebos.thoth.content.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FragmentTest {

  @Test
  public void testFragment() {
    String text = "Fragment text";
    Fragment fragment = new Fragment(text);
    assertEquals(text, fragment.getText());
    assertEquals(text, fragment.toString());
    String otherText = "Other Fragment text";
    fragment.setText(otherText);
    assertEquals(otherText, fragment.getText());
  }

}
