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
