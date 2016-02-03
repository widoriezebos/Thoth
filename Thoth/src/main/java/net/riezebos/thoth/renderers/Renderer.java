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
package net.riezebos.thoth.renderers;

import java.io.OutputStream;
import java.util.Map;

import net.riezebos.thoth.content.skinning.Skin;
import net.riezebos.thoth.exceptions.RenderException;

public interface Renderer {

  String CONTEXTPATH_PARAMETER = "contextpath";
  String BRANCHURL_PARAMETER = "branchurl";
  String BRANCH_PARAMETER = "branch";
  String PATH_PARAMETER = "path";
  String TITLE_PARAMETER = "title";
  String BODY_PARAMETER = "body";
  String SKINBASE_PARAMETER = "skinbase";
  String REFRESH_PARAMETER = "refresh";

  public enum RenderResult {
    OK, NOT_FOUND, FORBIDDEN
  };

  public String getTypeCode();

  public String getContentType(Map<String, Object> arguments);

  public RenderResult execute(String branch, String path, Map<String, Object> arguments, Skin skin, OutputStream outputStream) throws RenderException;
}
