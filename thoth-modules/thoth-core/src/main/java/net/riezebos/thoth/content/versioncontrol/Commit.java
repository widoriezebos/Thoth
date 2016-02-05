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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.riezebos.thoth.Configuration;

/**
 * @author wido
 */
public class Commit {
  private String author;
  private Date timestamp;
  private String message;
  private String shortMessage;
  private List<Revision> revisions = new ArrayList<>();
  private String id;

  public Commit() {
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String getFormattedTimestamp() {
    Configuration configuration = Configuration.getInstance();
    SimpleDateFormat dateFormat = configuration.getDateFormat();
    return dateFormat.format(timestamp);
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message == null ? message : message.trim();
  }

  public List<Revision> getRevisions() {
    return revisions;
  }

  public void setShortMessage(String shortMessage) {
    this.shortMessage = shortMessage == null ? shortMessage : shortMessage.trim();
  }

  public String getShortMessage() {
    return shortMessage;
  }

  public void addRevision(Revision fileRevision) {
    this.revisions.add(fileRevision);
    fileRevision.setCommit(this);
  }

  @Override
  public String toString() {
    String revMessage = (getFormattedTimestamp() + " " + getAuthor() + ": " + getShortMessage()).trim() + "\n";
    for (Revision rev : getRevisions()) {
      revMessage += "  " + rev.toString() + "\n";
    }
    return revMessage;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
