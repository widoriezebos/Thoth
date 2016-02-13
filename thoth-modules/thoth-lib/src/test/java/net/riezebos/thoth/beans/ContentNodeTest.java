package net.riezebos.thoth.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import net.riezebos.thoth.markdown.filehandle.FileHandle;

public class ContentNodeTest {

  @Test
  public void test() {

    Date ts = new Date();
    long filesize = 100L;

    FileHandle mockedFile = mock(FileHandle.class);
    when(mockedFile.length()).thenReturn(filesize);
    when(mockedFile.lastModified()).thenReturn(ts.getTime());
    when(mockedFile.isDirectory()).thenReturn(false);

    FileHandle mockedFolder = mock(FileHandle.class);
    when(mockedFolder.length()).thenReturn(filesize);
    when(mockedFolder.lastModified()).thenReturn(ts.getTime());
    when(mockedFolder.isDirectory()).thenReturn(true);

    ContentNode fileNode1 = new ContentNode("/some/path1", mockedFile);
    ContentNode fileNode2 = new ContentNode("/some/path2", mockedFile);
    ContentNode folderNode1 = new ContentNode("/some/path1", mockedFolder);
    ContentNode folderNode2 = new ContentNode("/some/path2", mockedFolder);

    ArrayList<ContentNode> children = new ArrayList<ContentNode>();
    children.add(fileNode2);
    fileNode1.setChildren(children);

    assertTrue(!fileNode1.isFolder());
    assertTrue(folderNode1.isFolder());
    assertEquals("/some/path1", fileNode1.getPath());
    assertEquals("/some/path1", fileNode1.toString());
    assertEquals(filesize, fileNode1.getSize());
    assertEquals(ts, fileNode1.getDateModified());
    assertTrue(folderNode1.compareTo(folderNode2) < 1);
    assertTrue(fileNode1.compareTo(fileNode2) < 1);
    assertTrue(fileNode1.compareTo(folderNode1) < 1);
    assertTrue(fileNode1.getChildren().contains(fileNode2));

  }

}
