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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;

public class RawRenderer extends RendererBase implements Renderer {
  public static final String TYPE = "raw";

  public RawRenderer(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  public String getTypeCode() {
    return TYPE;
  }

  public String getContentType(Map<String, Object> arguments) {
    return "text/plain;charset=UTF-8";
  }

  public RenderResult execute(Identity identity, String context, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream)
      throws RenderException {
    try {

      Configuration configuration = getConfiguration();
      ContentManager contentManager = getContentManager(context);

      boolean isBook = configuration.isBook(path);
      Permission permission = isBook ? Permission.READ_BOOKS : Permission.READ_FRAGMENTS;

      if (!contentManager.getAccessManager().hasPermission(identity, path, permission))
        return RenderResult.FORBIDDEN;

      RenderResult result = RenderResult.OK;
      FileHandle fileHandle = contentManager.getFileHandle(path);
      if (fileHandle.isFile()) {
        MarkDownDocument markDownDocument = contentManager.getMarkDownDocument(path, suppressErrors(arguments), getCriticProcessingMode(arguments));
        String markdownSource = markDownDocument.getMarkdown();
        InputStream is = new ByteArrayInputStream(markdownSource.getBytes(("UTF-8")));
        IOUtils.copy(is, outputStream);
        return result;
      } else {
        result = RenderResult.NOT_FOUND;
      }
      return result;
    } catch (Exception e) {
      throw new RenderException(e);
    }
  }
}
