package net.riezebos.thoth.markdown.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DocumentNodeTest {

  @Test
  public void test() {
    DocumentNode node = new DocumentNode("absolutefilePath", "description", 0, 0);
    DocumentNode node2 = new DocumentNode("/absolutefilePath2#bookmark", null, 2, 2);
    DocumentNode node3 = new DocumentNode("/absolutefilePath3#bookmark", "", 3, 3);
    DocumentNode node4 = new DocumentNode("path/to/absolutefilePath4.md#bookmark", "descr", 4, 4);

    node.addChild(node2);
    node2.addChild(node3);
    node2.addChild(node3);

    assertEquals(2, node2.getLevel());
    assertEquals("/absolutefilePath2", node2.getPath());
    assertEquals(3, node.flatten(true).size());
    assertEquals(4, node.flatten(false).size());

    String string = node.toString();
    assertEquals("/absolutefilePath\n" + //
        "  /absolutefilePath2\n" + //
        "    /absolutefilePath3\n" + //
        "    /absolutefilePath3", string);

    assertEquals("/path/to", node4.getFolder());
    assertEquals("absolutefilePath4.md", node4.getFileName());
    assertEquals("descr", node4.getDescription());
    assertEquals(4, node4.getIncludePosition());
  }

}
