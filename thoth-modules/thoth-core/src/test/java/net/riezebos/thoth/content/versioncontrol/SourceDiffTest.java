/* Copyright (c) 2020 W.T.J. Riezebos
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
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.util.diff_match_patch.Diff;

public class SourceDiffTest {

  @Test
  public void test() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat(Configuration.DEFAULT_TIMESTAMP_FMT);
    Date timeModified = sdf.parse("01-01-2016 13:14:00");
    String oldSource = "line1\nlineA\nline3";
    String newSource = "line1\nline2\nline3\nline4";
    String author = "Wido Riezebos";
    String commitMessage = "Changed line 2 and added line 4";
    SourceDiff diff = new SourceDiff(author, oldSource, newSource, timeModified);
    diff.setCommitMessage(commitMessage);

    assertEquals(timeModified, diff.getTimeModified());
    assertEquals(oldSource, diff.getOldSource());
    assertEquals(newSource, diff.getNewSource());
    assertEquals(author, diff.getAuthor());
    assertEquals(commitMessage, diff.getCommitMessage());
    assertEquals("01-01-2016 13:14:00: Wido Riezebos", diff.toString());

    List<Diff> diffs = diff.getDiffs();
    String log = String.valueOf(diffs);
    assertTrue(log.indexOf("Diff(DELETE,\"A\")") != -1);
    assertTrue(log.indexOf("Diff(INSERT,\"2\")") != -1);
    assertTrue(log.indexOf("Diff(EQUAL,\"¶line3\")") != -1);
    assertTrue(log.indexOf("Diff(INSERT,\"¶line4\")") != -1);
  }

}
