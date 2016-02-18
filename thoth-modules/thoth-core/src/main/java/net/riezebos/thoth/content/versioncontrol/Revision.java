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

import net.riezebos.thoth.util.ThothUtil;

public class Revision {
  public enum Action {
    ADD, MODIFY, DELETE, RENAME, COPY;
  };

  private Commit commit;
  private Action action;
  private String path;
  private String revisionMessage;
  private String commitId;

  public Revision(Action action, String path) {
    setPath(ThothUtil.prefix(path, "/"));
    setAction(action);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public void setCommit(Commit commit) {
    this.commit = commit;
  }

  public String getFileName() {
    return ThothUtil.getFileName(path);
  }

  @Override
  public String toString() {
    return action + ": " + getPath();
  }

  public void setMessage(String commitMessage) {
    this.revisionMessage = commitMessage == null ? commitMessage : commitMessage.trim();
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  public String getCommitId() {
    if (commitId != null)
      return commitId;
    return commit == null ? null : commit.getId();
  }

  public String getAuthor() {
    return commit == null ? null : commit.getAuthor();
  }

  public Date getTimestamp() {
    return commit == null ? null : commit.getTimestamp();
  }

  public String getMessage() {
    if (revisionMessage != null)
      return revisionMessage;
    return commit == null ? null : commit.getMessage();
  }

  public String getShortMessage() {
    return commit == null ? null : commit.getShortMessage();
  }
}
