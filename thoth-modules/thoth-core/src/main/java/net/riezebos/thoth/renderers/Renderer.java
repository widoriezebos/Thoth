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

import net.riezebos.thoth.commands.CommandOperation;
import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;
import net.riezebos.thoth.user.Identity;

public interface Renderer {

  String CONTEXTPATH_PARAMETER = "contextpath";
  String CONTEXTURL_PARAMETER = "contexturl";
  String CONTEXT_PARAMETER = "context";
  String PATH_PARAMETER = "path";
  String TITLE_PARAMETER = "title";
  String BODY_PARAMETER = "body";
  String SKINBASE_PARAMETER = "skinbase";
  String IDENTITY = "identity";
  String USER_FULL_NAME = "userfullname";
  String LOGGED_IN = "loggedin";
  String REFRESH_PARAMETER = "refresh";
  String LIBRARY_ROOT = "libraryroot";
  String LIBRARY_URL = "libraryurl";
  String SKIN = "skin";
  String TODAY = "today";
  String NOW = "now";
  String OUTPUT_FORMATS = "outputFormats";
  String PERMISSIONS = "permissions";

  public String getTypeCode();

  public String getContentType(Map<String, Object> arguments);

  public RenderResult execute(Identity identity, String context, String path, CommandOperation operation, Map<String, Object> arguments, Skin skin, OutputStream outputStream)
      throws RenderException;

}
