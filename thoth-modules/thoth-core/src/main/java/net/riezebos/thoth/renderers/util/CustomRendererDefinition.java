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

import org.apache.commons.lang3.StringUtils;

public class CustomRendererDefinition {

  private String extension;
  private String contentType;
  private String sourceRenderer;
  private String commandLine;

  public CustomRendererDefinition(String extension, String contentType, String sourceRenderer, String commandLine) {
    super();
    this.extension = extension;
    this.contentType = contentType;
    this.sourceRenderer = sourceRenderer;
    this.commandLine = commandLine;
    validate();
  }

  public void validate() {
    if (StringUtils.isBlank(extension))
      throw new IllegalArgumentException("The extension of renderer " + contentType + " is not set");
    if (StringUtils.isBlank(commandLine))
      throw new IllegalArgumentException("The commandLine of renderer " + contentType + " is not set");
  }

  public String getExtension() {
    return extension;
  }

  public String getSourceRenderer() {
    return sourceRenderer;
  }

  public String getContentType() {
    return contentType;
  }

  public String getCommandLine() {
    return commandLine;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((commandLine == null) ? 0 : commandLine.hashCode());
    result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
    result = prime * result + ((extension == null) ? 0 : extension.hashCode());
    result = prime * result + ((sourceRenderer == null) ? 0 : sourceRenderer.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CustomRendererDefinition other = (CustomRendererDefinition) obj;
    if (commandLine == null) {
      if (other.commandLine != null)
        return false;
    } else if (!commandLine.equals(other.commandLine))
      return false;
    if (contentType == null) {
      if (other.contentType != null)
        return false;
    } else if (!contentType.equals(other.contentType))
      return false;
    if (extension == null) {
      if (other.extension != null)
        return false;
    } else if (!extension.equals(other.extension))
      return false;
    if (sourceRenderer == null) {
      if (other.sourceRenderer != null)
        return false;
    } else if (!sourceRenderer.equals(other.sourceRenderer))
      return false;
    return true;
  }

}
