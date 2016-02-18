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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.util.diff_match_patch;
import net.riezebos.thoth.util.diff_match_patch.Diff;

/**
 * @author wido
 */
public class SourceDiff {
  private String oldSource;
  private String newSource;
  private String author;
  private Date timeModified;
  private String commitMessage;

  public SourceDiff(String author, String oldSource, String newSource, Date timeModified) {
    setAuthor(author);
    setOldSource(oldSource);
    setNewSource(newSource);
    setTimeModified(timeModified);
  }

  public String getOldSource() {
    return oldSource;
  }

  public void setOldSource(String oldSource) {
    this.oldSource = oldSource;
  }

  public void setNewSource(String newSource) {
    this.newSource = newSource;
  }

  public String getNewSource() {
    return newSource;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Date getTimeModified() {
    return timeModified;
  }

  public void setTimeModified(Date timeModified) {
    this.timeModified = timeModified;
  }

  public String getCommitMessage() {
    return commitMessage;
  }

  public void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }

  public List<Diff> getDiffs() {
    diff_match_patch dmp = new diff_match_patch();
    LinkedList<Diff> diffs = dmp.diff_main(oldSource == null ? "" : oldSource, newSource == null ? "" : newSource);
    dmp.diff_cleanupSemantic(diffs);
    return diffs;
  }

  @Override
  public String toString() {
    SimpleDateFormat sdf = new SimpleDateFormat(Configuration.DEFAULT_TIMESTAMP_FMT);
    String timestamp = getTimeModified() == null ? "null" : sdf.format(getTimeModified());

    return timestamp + ": " + getAuthor();
  }
}
