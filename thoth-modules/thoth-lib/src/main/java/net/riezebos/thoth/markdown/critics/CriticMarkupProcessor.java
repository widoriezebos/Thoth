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
package net.riezebos.thoth.markdown.critics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Supports CriticMarkup as specified by https://github.com/CriticMarkup/CriticMarkup-toolkit/
 *
 * @author wido
 */
public class CriticMarkupProcessor {

  public static final String INSERTION_PATTERN = "\\{\\+\\+(.*?)\\+\\+\\}";
  public static final String DELETION_PATTERN = "\\{\\-\\-(.*?)\\-\\-\\}";
  public static final String SUBSTITUTION_PATTERN = "\\{\\~\\~(.*?)\\~\\>(.*?)\\~\\~\\}";
  public static final String HIGHLIGHT_PATTERN = "\\{\\=\\=(.*?)\\=\\=\\}";
  public static final String COMMENT_PATTERN = "\\{\\>\\>(.*?)\\<\\<\\}";

  private Pattern addition = Pattern.compile(INSERTION_PATTERN, Pattern.DOTALL);
  private Pattern deletion = Pattern.compile(DELETION_PATTERN, Pattern.DOTALL);
  private Pattern substitution = Pattern.compile(SUBSTITUTION_PATTERN, Pattern.DOTALL);
  private Pattern highlight = Pattern.compile(HIGHLIGHT_PATTERN, Pattern.DOTALL);
  private Pattern comment = Pattern.compile(COMMENT_PATTERN, Pattern.DOTALL);

  public String processCritics(String source, CriticProcessingMode processingMode) {

    if (processingMode != CriticProcessingMode.DO_NOTHING) {
      source = translate(source, addition, new InsertTranslator(processingMode));
      source = translate(source, deletion, new DeleteTranslator(processingMode));
      source = translate(source, substitution, new SubstitutionTranslator(processingMode));
      source = translate(source, comment, new CommentTranslator(processingMode));
      source = translate(source, highlight, new HighlightTranslator(processingMode));
    }
    return source;
  }

  protected String translate(String source, Pattern pattern, TranslateAction action) {
    Matcher matcher = pattern.matcher(source);
    int idx = 0;
    StringBuilder result = new StringBuilder();
    while (matcher.find(idx)) {
      int start = matcher.start();
      int end = matcher.end();
      result.append(source.substring(idx, start));
      result.append(action.translate(matcher));
      idx = end;
    }
    if (idx < source.length())
      result.append(source.substring(idx));
    source = result.toString();
    return source;
  }
}
