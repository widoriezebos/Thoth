/* Copyright (c) 2016 W.T.J. Riezebos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.riezebos.thoth.content.versioncontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.content.versioncontrol.Revision.Action;

public class CommitTest {

  @Test
  public void testRevision() {
    String commitId = "1234567890asdfgh/some/path.md";
    String shortMessage = "Changed";
    Date date = new Date();
    String message = "Changed something";
    String author = "Wido Riezebos";

    Commit commit = new Commit();
    commit.setAuthor(author);
    commit.setId(commitId);
    commit.setMessage(message);
    commit.setShortMessage(shortMessage);
    commit.setTimestamp(date);

    Revision revision = new Revision(Action.ADD, "main/Main.md");
    revision.setCommitId(commitId);
    assertEquals("/main/Main.md", revision.getPath());
    assertEquals(commitId, revision.getCommitId());
    assertEquals("Main.md", revision.getFileName());
    assertEquals("ADD: /main/Main.md", revision.toString());
    assertEquals(Action.ADD, revision.getAction());
    assertNull(revision.getMessage());
    assertNull(revision.getShortMessage());
    assertNull(revision.getAuthor());
    assertNull(revision.getTimestamp());
    revision.setMessage(message);
    assertEquals(message, revision.getMessage());
    commit.addRevision(revision);
    assertEquals(author, revision.getAuthor());
    assertEquals(date, revision.getTimestamp());
    assertEquals(commitId, revision.getCommitId());
    assertEquals(message, revision.getMessage());
    assertEquals(shortMessage, revision.getShortMessage());

    Revision revision2 = new Revision(Action.DELETE, "main/Main.md");
    commit.addRevision(revision2);
    assertEquals(author, revision2.getAuthor());
    assertEquals(date, revision2.getTimestamp());
    assertEquals(commitId, revision2.getCommitId());
    assertEquals(message, revision2.getMessage());
    assertEquals(shortMessage, revision2.getShortMessage());

    assertEquals(2, commit.getRevisions().size());
    String report = commit.toString();
    assertTrue(report.indexOf("ADD: /main/Main.md") != -1);
    assertTrue(report.indexOf("DELETE: /main/Main.md") != -1);
  }

  @Test
  public void testSort() {
    long now = System.currentTimeMillis();

    Commit commit1 = new Commit();
    commit1.setAuthor("Wido Riezebos");
    commit1.setId("1234567890asdfgh/some/path.md");
    commit1.setMessage("Changed something");
    commit1.setShortMessage("Changed");
    commit1.setTimestamp(new Date(now));

    Commit commit2 = new Commit();
    commit2.setAuthor("Wido Riezebos");
    commit2.setId("0987654321asdfgh/some/other/path.md");
    commit2.setMessage("Deleted something");
    commit2.setShortMessage("Deleted");
    commit2.setTimestamp(new Date(now + 1000));

    Commit commit3 = new Commit();
    commit3.setAuthor("Wido Riezebos");
    commit3.setId("asdfghjkl34567cvbn/some/other/path.md");
    commit3.setMessage("Updated something");
    commit3.setShortMessage("Updated");
    commit3.setTimestamp(new Date(now + 2000));

    List<Commit> lst = new ArrayList<>();
    lst.add(commit3);
    lst.add(commit1);
    lst.add(commit2);
    Collections.sort(lst, new CommitComparator());
    assertEquals(commit3, lst.get(0));
    assertEquals(commit2, lst.get(1));
    assertEquals(commit1, lst.get(2));

  }
}
