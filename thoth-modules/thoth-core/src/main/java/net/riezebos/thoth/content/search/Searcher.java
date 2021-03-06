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

import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.content.AccessManager;
import net.riezebos.thoth.content.ContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.SearchException;
import net.riezebos.thoth.markdown.critics.CriticProcessingMode;
import net.riezebos.thoth.markdown.util.DocumentNode;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.user.Permission;
import net.riezebos.thoth.util.PagedList;
import net.riezebos.thoth.util.ThothCoreUtil;
import net.riezebos.thoth.util.ThothUtil;

public class Searcher {
  private static final Logger LOG = LoggerFactory.getLogger(Searcher.class);

  private ContentManager contentManager;

  public Searcher(ContentManager contentManager) {
    this.contentManager = contentManager;
  }

  public PagedList<SearchResult> search(Identity identity, String queryExpression, int pageNumber, int pageSize) throws SearchException {
    try {
      IndexReader reader = getIndexReader(contentManager);
      IndexSearcher searcher = getIndexSearcher(reader);
      Analyzer analyzer = new StandardAnalyzer();

      // We might need to restrict the results to books of the user does not have access to fragments:
      AccessManager accessManager = contentManager.getAccessManager();
      boolean booksOnly = !accessManager.hasPermission(identity, "", Permission.READ_FRAGMENTS);
      if (booksOnly) {
        queryExpression = Indexer.INDEX_TYPE + ":" + Indexer.TYPE_DOCUMENT + " AND (" + queryExpression + ")";
      }

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

          String type = document.get(Indexer.INDEX_TYPE);
          if (Indexer.TYPE_DOCUMENT.equals(type) || Indexer.TYPE_FRAGMENT.equals(type)) {
            searchResult.setResource(false);

            try {
              MarkDownDocument markDownDocument = contentManager.getMarkDownDocument(documentPath, true, CriticProcessingMode.DO_NOTHING);
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
            } catch (FileNotFoundException e) {
              LOG.warn("Index contains an invalid file reference); probably need to reindex to get rid of this. File: " + e.getMessage());
            }
          } else {
            searchResult.setResource(true);
            String extension = ThothUtil.getExtension(documentPath);
            searchResult.setImage(getConfiguration().isImageExtension(extension));

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

  protected IndexSearcher getIndexSearcher(IndexReader reader) {
    IndexSearcher searcher = new IndexSearcher(reader);
    return searcher;
  }

  protected IndexReader getIndexReader(ContentManager contentManager) throws ContextNotFoundException, IOException {
    String indexFolder = contentManager.getIndexFolder();
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexFolder)));
    return reader;
  }

  protected void linkBooks(List<SearchResult> searchResults) throws ContentManagerException {

    Map<String, List<String>> reverseIndexIndirect = contentManager.getReverseIndex(true);

    List<String> bookExtensions = getConfiguration().getBookExtensions();
    for (SearchResult searchResult : searchResults) {
      String document = searchResult.getDocument();
      List<String> list = reverseIndexIndirect.get(ThothUtil.prefix(document, "/"));
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

  protected Configuration getConfiguration() {
    return contentManager.getConfiguration();
  }

}
