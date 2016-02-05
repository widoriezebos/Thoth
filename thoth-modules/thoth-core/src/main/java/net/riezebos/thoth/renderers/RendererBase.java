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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.util.ThothCoreUtil;

public abstract class RendererBase implements Renderer {

  private static final String VELOCITY_HELPER = "thothutil";
  private static final String VELOCITY_PROPERTIES = "net/riezebos/thoth/velocity.properties";

  @Override
  public String getContentType(Map<String, Object> arguments) {
    return asJson(arguments) ? "application/json;charset=UTF-8" : "text/html;charset=UTF-8";
  }

  protected MarkDownDocument getMarkDownDocument(String branch, String path) throws IOException, ContentManagerException {
    return getContentManager().getMarkDownDocument(branch, path);
  }

  protected ContentManager getContentManager() throws ContentManagerException {
    return ContentManagerFactory.getContentManager();
  }

  protected String getFileSystemPath(String branch, String path) throws BranchNotFoundException, IOException, ContentManagerException {
    return getContentManager().getFileSystemPath(branch, path);
  }

  protected String getString(Map<String, Object> arguments, String key) {
    Object value = arguments.get(key);
    if (value == null || StringUtils.isBlank(String.valueOf(value)))
      return null;
    return String.valueOf(value);
  }

  protected Integer getInteger(Map<String, Object> arguments, String key) {
    String stringValue = getString(arguments, key);
    if (StringUtils.isBlank(stringValue))
      return null;
    return Integer.parseInt(stringValue);
  }

  protected boolean asJson(Map<String, Object> arguments) {
    String mode = getString(arguments, "mode");
    boolean asJson = "json".equals(mode);
    return asJson;
  }

  protected void executeJson(Map<String, Object> variables, OutputStream outputStream) throws ServletException {
    try {
      boolean prettyPrintJson = Configuration.getInstance().isPrettyPrintJson();
      ObjectMapper mapper = new ObjectMapper();
      ObjectWriter writer = prettyPrintJson ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
      writer.writeValue(outputStream, variables);
    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  protected void renderTemplate(String template, String branch, Map<String, Object> variables, OutputStream outputStream)
      throws ContentManagerException, IOException, UnsupportedEncodingException {

    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")), true)) {
      VelocityContext context = new VelocityContext(variables);
      context.put(VELOCITY_HELPER, new ThothCoreUtil());
      VelocityEngine engine = new VelocityEngine();
      Properties properties = new Properties();
      properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(VELOCITY_PROPERTIES));
      engine.init(properties);
      engine.getTemplate(template).merge(context, writer);
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }

}
