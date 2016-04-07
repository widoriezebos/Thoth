package net.riezebos.thoth.renderers.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class CustomRendererDefinitionTest {

  @Test
  public void test() {
    
    CustomRendererDefinition def1 = new CustomRendererDefinition("ext1", "contentType", "src", "commandLine");
    CustomRendererDefinition def2 = new CustomRendererDefinition("ext1", "contentType", "src", "commandLine");
    CustomRendererDefinition def3 = new CustomRendererDefinition("ext2", "contentType2", "src2", "commandLine2");
    
    assertEquals(def1, def2);
    assertEquals(def1.hashCode(), def2.hashCode());
    def2.validate();
    assertNotEquals(def3, def2);
  }

}
