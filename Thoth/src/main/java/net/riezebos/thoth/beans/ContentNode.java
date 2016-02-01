/*
 * Copyright (c) 2015 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 * 
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including 
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package net.riezebos.thoth.beans;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.riezebos.thoth.Configuration;

public class ContentNode implements Comparable<ContentNode> {
  private String path;
  private boolean isFolder;
  private Date dateModified;
  private long size;
  private List<ContentNode> children = new ArrayList<>();

  public ContentNode(String path, File file) {
    super();
    this.path = path;
    this.isFolder = file.isDirectory();
    this.dateModified = new Date(file.lastModified());
    this.size = isFolder ? 0 : file.length();
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean isFolder() {
    return isFolder;
  }

  public void setFolder(boolean isFolder) {
    this.isFolder = isFolder;
  }

  public List<ContentNode> getChildren() {
    return children;
  }

  public void setChildren(List<ContentNode> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    return getPath();
  }

  public Date getDateModified() {
    return dateModified;
  }

  public String getDateModifiedString() {
    SimpleDateFormat dateFormat = Configuration.getInstance().getDateFormat();
    return dateFormat.format(dateModified);
  }

  public long getSize() {
    return size;
  }

  @Override
  public int compareTo(ContentNode o) {
    int result = new Boolean(isFolder).compareTo(o.isFolder);
    if (result != 0)
      return result;

    return getPath().compareTo(o.getPath());
  }
}
