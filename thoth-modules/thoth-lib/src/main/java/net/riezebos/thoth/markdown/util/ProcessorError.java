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
package net.riezebos.thoth.markdown.util;

import java.io.Serializable;

import net.riezebos.thoth.util.ThothUtil;

public class ProcessorError implements Serializable, Comparable<ProcessorError> {

  private static final long serialVersionUID = 1L;

  private LineInfo currentLineInfo;
  private String errorMessage;

  public ProcessorError(LineInfo currentLineInfo, String errorMessage) {
    setCurrentLineInfo(currentLineInfo);
    setErrorMessage(errorMessage);
  }

  public void setCurrentLineInfo(LineInfo currentLineInfo) {
    this.currentLineInfo = currentLineInfo == null ? null : currentLineInfo.clone();
  }

  public LineInfo getCurrentLineInfo() {
    return currentLineInfo;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getDescription() {
    return (currentLineInfo == null ? "" : currentLineInfo + ": ") + errorMessage;
  }

  public boolean getFileRelated() {
    return currentLineInfo != null;
  }

  public String getFile() {
    return currentLineInfo.getFile();
  }

  public int getLine() {
    return currentLineInfo.getLine();
  }

  @Override
  public String toString() {
    return getDescription();
  }

  @Override
  public int compareTo(ProcessorError o) {
    int result = ThothUtil.safeCompare(getFile(), o.getFile());
    if (result != 0)
      return result;
    result = ThothUtil.safeCompare(getLine(), o.getLine());
    if (result != 0)
      return result;
    result = ThothUtil.safeCompare(getDescription(), o.getDescription());
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((currentLineInfo == null) ? 0 : currentLineInfo.hashCode());
    result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
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
    ProcessorError other = (ProcessorError) obj;
    if (currentLineInfo == null) {
      if (other.currentLineInfo != null)
        return false;
    } else if (!currentLineInfo.equals(other.currentLineInfo))
      return false;
    if (errorMessage == null) {
      if (other.errorMessage != null)
        return false;
    } else if (!errorMessage.equals(other.errorMessage))
      return false;
    return true;
  }
}
