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

import org.apache.commons.lang3.StringUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TextNode;

public class CustomHtmlSerializer extends ToHtmlSerializer {

  private boolean inTable = false;

  public CustomHtmlSerializer(LinkRenderer linkRenderer) {
    super(linkRenderer);
  }

  public void visit(HeaderNode node) {
    // Headers might be tokenized; so we need to append them together.
    String combinedName = "";
    for (Node child : node.getChildren()) {
      if (child instanceof TextNode) {
        TextNode textNode = (TextNode) child;
        String text = textNode.getText();
        if (text != null && text.trim().length() > 0) {
          combinedName += text.trim();
        }
      }
    }
    if (StringUtils.isNoneBlank(combinedName)) {
      String ref = ThothUtil.encodeBookmark(combinedName);

      // We add a style to the link because PDF generation requires a block style to be able to link to
      printer.print("<a name=\"" + ref + "\" style=\"display:block\"></a>");

      // Also add abookmark that excludes any (potentially) generated numbers as a prefix of the title
      int idx = 0;
      for (int i = 0; i < ref.length(); i++, idx++) {
        if (!Character.isDigit(ref.charAt(i)) && ref.charAt(i) != '.')
          break;
      }
      if (idx != 0) {
        String alternate = ref.substring(idx).trim();
        printer.print("<a name=\"" + alternate + "\" style=\"display:block\"></a>");
      }
    }
    printBreakBeforeTag(node, "h" + node.getLevel());
  }

  public void visit(TableNode node) {
    inTable = true;
    super.visit(node);
    inTable = false;
  }

  @Override
  public void visit(TextNode node) {
    if (inTable) {
      String text = node.getText();
      text = text.replaceAll("([/\\.\\,\\\\])", "$1<wbr>") + "<wbr>";
      if (abbreviations.isEmpty()) {
        printer.print(text);
      } else {
        printWithAbbreviations(text);
      }
    } else
      super.visit(node);
  }
}
