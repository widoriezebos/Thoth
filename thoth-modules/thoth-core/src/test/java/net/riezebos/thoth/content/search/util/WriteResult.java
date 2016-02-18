package net.riezebos.thoth.content.search.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

public class WriteResult {
  private Term term;
  private Document document;

  public WriteResult(Term term, Document document) {
    this.term = term;
    this.document = document;
  }

  public Term getTerm() {
    return term;
  }

  public Document getDocument() {
    return document;
  }
}
