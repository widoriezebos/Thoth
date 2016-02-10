package net.riezebos.thoth.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.riezebos.thoth.markdown.util.DocumentNode;
import net.riezebos.thoth.markdown.util.LineInfo;
import net.riezebos.thoth.markdown.util.ProcessorError;

public class MarkDownDocumentTest {

  @Test
  public void test() {
    Date now = new Date();
    DocumentNode documentStructure = new DocumentNode("a/b/c", "description", 0, 0);
    List<ProcessorError> errors = new ArrayList<ProcessorError>();
    ProcessorError processorError = new ProcessorError(new LineInfo("file", 0), "errorMessage");
    Map<String, String> metatags = new HashMap<String, String>();
    String markdown = "#title\nSometext";
    MarkDownDocument markDownDocument = new MarkDownDocument(markdown, metatags, errors, documentStructure);

    markDownDocument.setLastModified(now);
    assertEquals("/a/b/c", markDownDocument.getPath());
    assertEquals("c", markDownDocument.getName());
    assertEquals("c", markDownDocument.getTitle());
    assertEquals(now, markDownDocument.getLastModified());
    assertFalse(markDownDocument.hasErrors());
    errors.add(processorError);
    assertTrue(markDownDocument.hasErrors());

    metatags.put("title", "New title");
    assertEquals("New title", markDownDocument.getTitle());
    assertEquals(documentStructure, markDownDocument.getDocumentStructure());
    assertTrue(markDownDocument.getMetatags().containsKey("title"));
    assertEquals(1, markDownDocument.getErrors().size());
    assertEquals(markdown, markDownDocument.getMarkdown());

  }

}
