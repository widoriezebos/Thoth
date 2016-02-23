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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;
import net.riezebos.thoth.util.ThothUtil;

public class CustomRenderer extends HtmlRenderer implements Renderer {
  private static final Logger LOG = LoggerFactory.getLogger(CustomRenderer.class);

  private String typeCode;
  private String contentType;
  private String commandLine;

  public CustomRenderer(ThothEnvironment thothEnvironment, CustomRendererDefinition definition) {
    super(thothEnvironment);
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

  public RenderResult execute(String context, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException {
    try {

      ByteArrayOutputStream html = new ByteArrayOutputStream();
      RenderResult htmlRenderResult = renderHtml(context, path, arguments, skin, html);
      if (htmlRenderResult != RenderResult.OK)
        return htmlRenderResult;

      RenderResult result = RenderResult.OK;
      File tempFile = File.createTempFile("thothtemp", "." + typeCode);
      File tempHtml = File.createTempFile("thothhtml", ".html");
      try {
        IOUtils.copy(new ByteArrayInputStream(html.toByteArray()), new FileOutputStream(tempHtml));

        Configuration configuration = getConfiguration();
        String url = (configuration.getLocalHostUrl() + context + "/" + path).replaceAll(" ", "%20");

        arguments.put("input", tempHtml.getAbsolutePath());
        arguments.put("url", url);
        arguments.put("output", tempFile.getAbsolutePath());
        String command = ThothUtil.replaceKeywords(getCommandLine(configuration), arguments);

        String workingFolder = getConfiguration().getWorkspaceLocation() + context + ThothUtil.prefix(ThothUtil.getFolder(path), "/");
        File contextLocation = new File(workingFolder);
        if (!contextLocation.isDirectory())
          contextLocation = null;

        execute(command, arguments, contextLocation);

        try (FileInputStream fis = new FileInputStream(tempFile)) {
          IOUtils.copy(fis, outputStream);
        }
      } finally {
        tempFile.delete();
        tempHtml.delete();
      }
      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

  protected void execute(String command, Map<String, Object> arguments, File workingFolder) throws IOException {
    LOG.debug("Executing " + command);

    List<String> argsList = new ArrayList<>();
    for (Entry<String, Object> entry : arguments.entrySet()) {
      argsList.add(entry.getKey() + "=" + entry.getValue());
    }

    String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[argsList.size()]);

    String line;
    Process p = Runtime.getRuntime().exec(command, args, workingFolder);
    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
    while ((line = input.readLine()) != null) {
      LOG.debug(line);
    }
    input.close();
  }

}
