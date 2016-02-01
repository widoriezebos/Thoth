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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.CustomRendererDefinition;
import net.riezebos.thoth.content.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.util.RendererBase;
import net.riezebos.thoth.util.ThothUtil;

public class CustomRenderer extends RendererBase implements Renderer {
  private static final Logger LOG = LoggerFactory.getLogger(CustomRenderer.class);

  private String typeCode;
  private String contentType;
  private String commandLine;

  public CustomRenderer() {
  }

  public CustomRenderer(CustomRendererDefinition definition) {
    setTypeCode(definition.getExtension());
    setContentType(definition.getContentType());
    setCommandLine(definition.getCommandLine());
  }

  public String getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(String typeCode) {
    this.typeCode = typeCode;
  }

  @Override
  public String getContentType(Map<String, Object> arguments) {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  protected String getCommandLine(Configuration configuration) {
    return commandLine;
  }

  public void setCommandLine(String commandLine) {
    this.commandLine = commandLine;
  }

  public RenderResult execute(String branch, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {
    try {
      RenderResult result = RenderResult.OK;

      String absolutePath = getFileSystemPath(branch, path);
      if (absolutePath == null) {
        result = RenderResult.FORBIDDEN;
      } else {
        Configuration configuration = Configuration.getInstance();
        String url = (configuration.getLocalHostUrl() + branch + "/" + path).replaceAll(" ", "%20");

        File tempFile = File.createTempFile("thothtemp", "." + typeCode);

        arguments.put("url", url);
        arguments.put("output", tempFile.getAbsolutePath());
        String command = ThothUtil.replaceKeywords(getCommandLine(configuration), arguments);

        execute(command);

        try (FileInputStream fis = new FileInputStream(tempFile)) {
          IOUtils.copy(fis, outputStream);
        }
        tempFile.delete();
      }
      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  protected void execute(String command) throws IOException {
    LOG.debug("Executing " + command);

    String line;
    Process p = Runtime.getRuntime().exec(command);
    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
    while ((line = input.readLine()) != null) {
      LOG.debug(line);
    }
    input.close();
  }

}
