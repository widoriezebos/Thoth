package net.riezebos.thoth.renderers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCustomRenderer extends CustomRenderer {

  private String executedCommand = null;

  public String getExecutedCommand() {
    return executedCommand;
  }

  public void setExecutedCommand(String executedCommand) {
    this.executedCommand = executedCommand;
  }

  @Override
  protected void execute(String command) throws IOException {
    executedCommand = command;

    Pattern pattern = Pattern.compile("(\\w+)\\=\\{(.*?)\\}");

    Matcher matcher = pattern.matcher(command);
    int idx = 0;
    if (matcher.find(idx)) {
      String key = matcher.group(1);
      String value = matcher.group(2);
      if ("output".equals(key)) {
        File tempFile = new File(value);
        PrintWriter writer = new PrintWriter(new FileOutputStream(tempFile));
        writer.println("rendered");
        writer.close();
      }
      idx = matcher.end();
    }
  }
}
