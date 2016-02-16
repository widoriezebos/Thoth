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

import java.io.OutputStream;
import java.util.Map;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;

public interface Renderer {

  String CONTEXTPATH_PARAMETER = "contextpath";
  String CONTEXTURL_PARAMETER = "contexturl";
  String CONTEXT_PARAMETER = "context";
  String PATH_PARAMETER = "path";
  String TITLE_PARAMETER = "title";
  String BODY_PARAMETER = "body";
  String SKINBASE_PARAMETER = "skinbase";
  String REFRESH_PARAMETER = "refresh";
  String SKIN = "skin";
  String TODAY = "today";
  String NOW = "now";

  public enum RenderResult {
    OK, NOT_FOUND, FORBIDDEN
  };

  void setConfiguration(Configuration configuration);

  public String getTypeCode();

  public String getContentType(Map<String, Object> arguments);

  public RenderResult execute(String context, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException;
}
