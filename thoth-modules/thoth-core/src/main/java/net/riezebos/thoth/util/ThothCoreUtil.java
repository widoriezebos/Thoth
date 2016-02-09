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
package net.riezebos.thoth.util;

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;

import net.riezebos.thoth.configuration.ConfigurationFactory;

public class ThothCoreUtil extends ThothUtil {

  public static String escapeHtml(String html) {
    return StringEscapeUtils.escapeHtml(html);
  }

  public static String escapeHtmlExcept(String tag, String html) {
    String result = escapeHtml(html);
    result = result.replaceAll("&lt;" + tag + "&gt;", "<" + tag + ">");
    result = result.replaceAll("&lt;/" + tag + "&gt;", "</" + tag + ">");
    return result;
  }

  public static String formatDate(Date date) {
    if (date == null)
      return null;
    return ConfigurationFactory.getConfiguration().getTimestampFormat().format(date);
  }
}
