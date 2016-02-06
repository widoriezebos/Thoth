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
package net.riezebos.thoth.content.search;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.store.FSDirectory;

import net.riezebos.thoth.Configuration;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.SearchException;
import net.riezebos.thoth.markdown.util.DocumentNode;
import net.riezebos.thoth.util.PagedList;
import net.riezebos.thoth.util.ThothCoreUtil;

public class Searcher {

  private String context;

  protected Searcher(String context) {
    this.context = context;
  }

  public PagedList<SearchResult> search(String queryExpression, int pageNumber, int pageSize) throws SearchException {
    try {
      ContentManager contentManager = ContentManagerFactory.getContentManager();
      String indexFolder = contentManager.getIndexFolder(context);
      IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexFolder)));
      IndexSearcher searcher = new IndexSearcher(reader);
      Analyzer analyzer = new StandardAnalyzer();

      QueryParser parser = new QueryParser(Indexer.INDEX_CONTENTS, analyzer);
      Query query = parser.parse(queryExpression);

      // We add 1 to determine if there is more to be found after the current page
      int maxResults = pageSize * pageNumber + 1;
      TopDocs results = searcher.search(query, maxResults, Sort.RELEVANCE);
      ScoreDoc[] hits = results.scoreDocs;

      boolean hadMore = (hits.length == maxResults);

      List<SearchResult> searchResults = new ArrayList<>();
      int idx = 0;
      for (ScoreDoc scoreDoc : hits) {
        if (searchResults.size() == pageSize)
          break;
        idx++;
        if (idx >= (pageNumber - 1) * pageSize) {
          Document document = searcher.doc(scoreDoc.doc);
          IndexableField field = document.getField(Indexer.INDEX_PATH);
          String documentPath = field.stringValue();
          SearchResult searchResult = new SearchResult();
          searchResult.setIndexNumber((pageNumber - 1) * pageSize + idx);
          searchResult.setDocument(documentPath);

          if (Indexer.TYPE_DOCUMENT.equals(document.get(Indexer.INDEX_TYPE))) {
            searchResult.setResource(false);
            MarkDownDocument markDownDocument = contentManager.getMarkDownDocument(context, documentPath);
            String contents = markDownDocument.getMarkdown();

            SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
            Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query, Indexer.INDEX_CONTENTS));
            highlighter.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);

            TokenStream tokenStream = analyzer.tokenStream(Indexer.INDEX_CONTENTS, contents);

            TextFragment[] frags = highlighter.getBestTextFragments(tokenStream, contents, false, 99999);
            for (TextFragment frag : frags) {
              if ((frag != null) && (frag.getScore() > 0)) {
                String fragmentText = frag.toString();
                searchResult.addFragment(new Fragment(ThothCoreUtil.escapeHtmlExcept("B", fragmentText)));
              }
            }
          } else {
            searchResult.setResource(true);
            searchResult.addFragment(new Fragment(document.get(Indexer.INDEX_TITLE)));
          }
          searchResults.add(searchResult);
        }
      }
      reader.close();
      linkBooks(searchResults);
      PagedList<SearchResult> pagedList = new PagedList<>(searchResults, hadMore);
      return pagedList;
    } catch (Exception e) {
      throw new SearchException(e);
    }
  }

  protected void linkBooks(List<SearchResult> searchResults) throws ContentManagerException {

    SearchFactory searchFactory = SearchFactory.getInstance();
    Indexer indexer = searchFactory.getIndexer(context);
    Map<String, List<String>> reverseIndexIndirect = indexer.getReverseIndex(context, true);

    List<String> bookExtensions = Configuration.getInstance().getBookExtensions();
    for (SearchResult searchResult : searchResults) {
      String document = searchResult.getDocument();
      List<String> list = reverseIndexIndirect.get("/" + document);
      if (list != null) {
        for (String name : list) {
          int idx = name.lastIndexOf('.');
          if (idx != -1) {
            String ext = name.substring(idx + 1);
            if (bookExtensions.contains(ext)) {
              searchResult.addBookReference(new DocumentNode(name, name, 0, 0));
            }
          }
        }
      }
    }
  }

}
