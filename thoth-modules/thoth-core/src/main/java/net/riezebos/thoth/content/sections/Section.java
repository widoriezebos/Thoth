package net.riezebos.thoth.content.sections;

import java.util.ArrayList;
import java.util.List;

import net.riezebos.thoth.content.comments.Comment;

public class Section {
  private List<Section> subSections = new ArrayList<>();
  private List<Comment> comments = new ArrayList<>();
  private String path = null;
  private Section parent;

  public Section(String path) {
    this.path = path;
  }

  public Section() {
  }

  public boolean isFlatText() {
    return false;
  }

  public String getPath() {
    return path;
  }

  public void addSection(String text) {
    boolean handled = false;
    if (!subSections.isEmpty()) {
      Section latest = subSections.get(subSections.size() - 1);
      if (latest instanceof StringSection) {
        ((StringSection) latest).append(text);
        handled = true;
      }
    }
    if (!handled)
      addSection(new StringSection(text));
  }

  public void addSection(Section section) {
    subSections.add(section);
    section.setParent(this);
  }

  public List<Section> getSubSections() {
    return subSections;
  }

  protected void setParent(Section section) {
    parent = section;
  }

  public Section getParent() {
    return parent;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    subSections.stream().forEach(s -> sb.append(s.toString()));
    return sb.toString();
  }

  public String getLocalText() {
    return "";
  }

  public List<Comment> getComments() {
    return comments;
  }

  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }
}
