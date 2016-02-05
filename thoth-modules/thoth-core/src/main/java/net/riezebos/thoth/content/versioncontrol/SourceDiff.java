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

import java.util.Date;

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
    super();
    this.author = author;
    this.oldSource = oldSource;
    this.newSource = newSource;
    this.timeModified = timeModified;
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

  @Override
  public String toString() {
    return getTimeModified() + ": " + getAuthor();
  }
}
