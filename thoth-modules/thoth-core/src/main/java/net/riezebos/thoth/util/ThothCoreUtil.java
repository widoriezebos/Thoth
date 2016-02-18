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

import net.riezebos.thoth.configuration.Configuration;

public class ThothCoreUtil extends ThothUtil {

  Configuration configuration;

  public ThothCoreUtil(Configuration configuration) {
    this.configuration = configuration;
  }

  public static String escapeHtml(String html) {
    return StringEscapeUtils.escapeHtml(html);
  }

  public static String escapeHtmlExcept(String tag, String html) {
    String result = escapeHtml(html);
    result = result.replaceAll("&lt;" + tag + "&gt;", "<" + tag + ">");
    result = result.replaceAll("&lt;/" + tag + "&gt;", "</" + tag + ">");
    return result;
  }

  public String formatTimestamp(Date date) {
    if (date == null)
      return null;
    return configuration.getTimestampFormat().format(date);
  }

  public String formatDate(Date date) {
    if (date == null)
      return null;
    return configuration.getDateFormat().format(date);
  }

  public static String getVersion() {
    return getVersion(Version.CORE);
  }

  /**
   * Returns the number of UTF-8 characters, or -1 if the array does not contain a valid UTF-8 string. Overlong encodings, null characters, invalid Unicode
   * values, and surrogates are accepted.
   */
  public static int charLength(byte[] bytes) {
    int charCount = 0, expectedLen;

    for (int i = 0; i < bytes.length; i++) {
      charCount++;
      // Lead byte analysis
      if ((bytes[i] & 0b10000000) == 0b00000000)
        continue;
      else if ((bytes[i] & 0b11100000) == 0b11000000)
        expectedLen = 2;
      else if ((bytes[i] & 0b11110000) == 0b11100000)
        expectedLen = 3;
      else if ((bytes[i] & 0b11111000) == 0b11110000)
        expectedLen = 4;
      else if ((bytes[i] & 0b11111100) == 0b11111000)
        expectedLen = 5;
      else if ((bytes[i] & 0b11111110) == 0b11111100)
        expectedLen = 6;
      else
        return -1;

      // Count trailing bytes
      while (--expectedLen > 0) {
        if (++i >= bytes.length) {
          return -1;
        }
        if ((bytes[i] & 0b11000000) != 0b10000000) {
          return -1;
        }
      }
    }
    return charCount;
  }

}
