package net.riezebos.thoth.content.sections;

public class StringSection extends Section {

  private StringBuilder text;

  public StringSection(String text) {
    super();
    this.text = new StringBuilder(text);
  }

  @Override
  public boolean isFlatText() {
    return true;
  }

  @Override
  public String getPath() {
    Section parent = getParent();
    if (parent != null)
      return parent.getPath();
    else
      return super.getPath();
  }

  @Override
  public String toString() {
    return text.toString();
  }

  public void append(String additional) {
    text.append("\n");
    text.append(additional);
  }

  @Override
  public String getLocalText() {
    return text.toString();
  }

}
