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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.ThothUtil;

public abstract class RendererBase implements Renderer {

  private static final String CLASSPATH_PREFIX = "classpath:";

  protected MarkDownDocument getMarkDownDocument(String branch, String path) throws IOException, ContentManagerException {
    return getContentManager().getMarkDownDocument(branch, path);
  }

  protected ContentManager getContentManager() throws ContentManagerException {
    return ContentManagerFactory.getContentManager();
  }

  protected String getFileSystemPath(String branch, String path) throws BranchNotFoundException, IOException, ContentManagerException {
    return getContentManager().getFileSystemPath(branch, path);
  }

  protected void executeSimpleTemplate(String templateFileName, Map<String, Object> variables, OutputStream outputStream)
      throws BranchNotFoundException, ContentManagerException, IOException, ServletException {
    if (!StringUtils.isBlank(templateFileName)) {

      InputStream is = null;
      if (templateFileName.startsWith(CLASSPATH_PREFIX)) {
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(templateFileName.substring(CLASSPATH_PREFIX.length()));
      } else {
        File templateFile = new File(templateFileName);
        if (templateFile.exists())
          is = new FileInputStream(templateFile);
      }

      try (PrintWriter writer = new PrintWriter(outputStream)) {
        if (is == null) {
          writer.print("Template file " + templateFileName + " not found\n");
        } else {
          ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
          IOUtils.copy(is, bos);
          String template = ThothUtil.replaceKeywords(bos.toString("UTF-8"), variables);
          writer.print(template);
        }
      }
    }
  }
}
