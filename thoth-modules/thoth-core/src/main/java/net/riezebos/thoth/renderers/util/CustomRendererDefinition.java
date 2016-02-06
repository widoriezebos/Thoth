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
package net.riezebos.thoth.renderers.util;

public class CustomRendererDefinition {
  private String extension;
  private String contentType;
  private String commandLine;

  public CustomRendererDefinition(String extension, String contentType, String commandLine) {
    super();
    this.extension = extension;
    this.contentType = contentType;
    this.commandLine = commandLine;
  }

  public String getExtension() {
    return extension;
  }

  public String getContentType() {
    return contentType;
  }

  public String getCommandLine() {
    return commandLine;
  }

}