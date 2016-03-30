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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TextNode;

import net.riezebos.thoth.util.ThothCoreUtil;
import net.riezebos.thoth.util.ThothUtil;

public class CustomHtmlSerializer extends ToHtmlSerializer {

  public static final String MARKDOWN_TABLE_CLASS = "markdowntable";

  public CustomHtmlSerializer(LinkRenderer linkRenderer) {
    super(linkRenderer);
  }

  // Add class to a table to that Markdown based tables can be separated from any other tables present in the
  // resulting HTML.
  @Override
  public void visit(TableNode node) {
    currentTableNode = node;
    printIndentedTagWithClass(node, "table", MARKDOWN_TABLE_CLASS);
    currentTableNode = null;
  }

  // Add breaks at typical locations so that long lines in tables break better
  @Override
  public void visit(TextNode node) {
    if (currentTableNode != null) {
      String text = ThothCoreUtil.addHtmlBreaks(node.getText());
      if (abbreviations.isEmpty()) {
        printer.print(text);
      } else {
        printWithAbbreviations(text);
      }
    } else
      super.visit(node);
  }

  @Override
  public void visit(SpecialTextNode node) {
    super.visit(node);
    if (currentTableNode != null)
      printer.print("<wbr/>");
  }

  // Suppress printing <p></p> around inline HTML because that makes no sense (might break nesting)
  @Override
  public void visit(ParaNode node) {
    if (isInlineHtml(node))
      printBreakBeforeTag(node, "p");
    else
      visitChildren(node);
  }

  // Add a few extra bookmarks to the header (aliases)
  // Also: embed them inside the header to avoid mis-alignment when navigating
  // (Should be on not before or after the H-tag
  @Override
  public void visit(HeaderNode node) {
    String tag = "h" + node.getLevel();

    boolean startWasNewLine = printer.endsWithNewLine();
    printer.println();
    printer.print('<').print(tag).print('>');
    printer.println();
    writeBookmarks(node);
    visitChildren(node);
    printer.print('<').print('/').print(tag).print('>');
    if (startWasNewLine)
      printer.println();
  }

  protected void writeBookmarks(HeaderNode node) {
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
      String ref = ThothUtil.encodeBookmark(combinedName, false);
      writeBookmark(ref);

      // Also add a bookmark that excludes any (potentially) generated numbers as a prefix of the title
      int idx = 0;
      for (int i = 0; i < ref.length(); i++, idx++) {
        if (!Character.isDigit(ref.charAt(i)) && ref.charAt(i) != '.')
          break;
      }
      if (idx != 0) {
        String alternate = ref.substring(idx).trim();
        writeBookmark(alternate);
      }
    }
  }

  protected void writeBookmark(String alias) {
    printer.print("<a name=\"" + alias + "\" style=\"display:block\"></a>");
    if (!alias.equals(alias.toLowerCase()))
      writeBookmark(alias.toLowerCase());
  }

  protected void printIndentedTagWithClass(SuperNode node, String tag, String className) {
    printer.println().print('<').print(tag).print(" class=\"" + className + "\"").print('>').indent(+2);
    visitChildren(node);
    printer.indent(-2).println().print('<').print('/').print(tag).print('>');
  }

  protected boolean isInlineHtml(ParaNode node) {
    boolean makeParagraph = true;
    List<Node> children = node.getChildren();
    if (!children.isEmpty() && children.get(0) instanceof SuperNode) {
      Node superNode = children.get(0);
      List<Node> childrenOfSuper = superNode.getChildren();
      if (!childrenOfSuper.isEmpty()) {
        Node nestedChild = childrenOfSuper.get(0);
        makeParagraph = !(nestedChild instanceof InlineHtmlNode);
      }
    }
    return makeParagraph;
  }
}
