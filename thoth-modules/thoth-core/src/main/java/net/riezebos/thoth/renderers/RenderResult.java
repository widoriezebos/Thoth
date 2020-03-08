/* Copyright (c) 2020 W.T.J. Riezebos
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

import java.util.HashMap;
import java.util.Map;

public class RenderResult {

  public static RenderResult OK = OK(null);
  public static RenderResult NOT_FOUND = NOT_FOUND(null);
  public static RenderResult FORBIDDEN = FORBIDDEN(null);
  public static RenderResult LOGGED_IN = LOGGED_IN(null);
  public static RenderResult LOGGED_OUT = LOGGED_OUT(null);

  public enum RenderResultCode {
    OK, NOT_FOUND, FORBIDDEN, LOGGED_IN, LOGGED_OUT
  };

  private RenderResultCode code;
  private Map<String, Object> arguments = null;

  private RenderResult(RenderResultCode renderResultCode, Map<String, Object> arguments) {
    code = renderResultCode;
    this.arguments = arguments;
  }

  public RenderResultCode getCode() {
    return code;
  }

  @Override
  public String toString() {
    return String.valueOf(code);
  }

  public Map<String, Object> getArguments() {
    return arguments == null ? new HashMap<>() : arguments;
  }

  public static RenderResult OK(Map<String, Object> arguments) {
    return new RenderResult(RenderResultCode.OK, arguments);
  }

  public static RenderResult NOT_FOUND(Map<String, Object> arguments) {
    return new RenderResult(RenderResultCode.NOT_FOUND, arguments);
  }

  public static RenderResult FORBIDDEN(Map<String, Object> arguments) {
    return new RenderResult(RenderResultCode.FORBIDDEN, arguments);
  }

  public static RenderResult LOGGED_IN(Map<String, Object> arguments) {
    return new RenderResult(RenderResultCode.LOGGED_IN, arguments);
  }

  public static RenderResult LOGGED_OUT(Map<String, Object> arguments) {
    return new RenderResult(RenderResultCode.LOGGED_OUT, arguments);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((code == null) ? 0 : code.hashCode());
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
    RenderResult other = (RenderResult) obj;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    return true;
  }

  @SuppressWarnings("unchecked")
  public <T> T getArgument(String name) {
    return (T) arguments.get(name);
  }

}
