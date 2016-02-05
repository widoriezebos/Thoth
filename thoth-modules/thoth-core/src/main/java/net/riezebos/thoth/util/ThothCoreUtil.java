/*
 * Copyright (c) 2015 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 * 
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including 
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
 */
package net.riezebos.thoth.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;

import net.riezebos.thoth.Configuration;

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
    SimpleDateFormat sdf = new SimpleDateFormat(Configuration.getInstance().getDateFormatMask());
    return sdf.format(date);
  }
}
