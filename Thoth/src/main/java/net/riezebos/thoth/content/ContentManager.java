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
package net.riezebos.thoth.content;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.beans.BookClassification;
import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.exceptions.BranchNotFoundException;
import net.riezebos.thoth.exceptions.ContentManagerException;

public interface ContentManager {

  public String NATIVERESOURCES = "/nativeresources/";

  String refresh() throws ContentManagerException;

  MarkDownDocument getMarkDownDocument(String branch, String documentPath) throws IOException, BranchNotFoundException;

  Date getLatestRefresh();

  Date getLatestRefresh(String branch);

  void enableAutoRefresh();

  boolean isRefreshing();

  List<String> getBranches();

  String getBranchFolder(String branch) throws BranchNotFoundException;

  String getIndexFolder(String branch) throws BranchNotFoundException;

  String getReverseIndexFileName(String branch) throws BranchNotFoundException;

  public String getReverseIndexIndirectFileName(String branch) throws BranchNotFoundException;

  String getErrorFileName(String branch) throws BranchNotFoundException;

  List<Commit> getLatestCommits(String branch, String path, int maxRevisons) throws ContentManagerException;

  public List<Commit> getCommits(String branch, String path, int startIdx, int endIdx) throws ContentManagerException;

  List<Book> getBooks(String branch) throws BranchNotFoundException, IOException;

  List<BookClassification> getClassification(List<Book> books, String metaTagName, String defaultValue);

  boolean accessAllowed(File file) throws IOException;

  String getFileSystemPath(String branch, String path) throws BranchNotFoundException, IOException;

  public SourceDiff getDiff(String branch, String id) throws ContentManagerException;

  void reindex();

  List<ContentNode> list(String branch, String path) throws BranchNotFoundException, IOException;

  public List<ContentNode> find(String branch, String fileSpec, boolean recursive) throws BranchNotFoundException, IOException;

  public String getInheritedPath(String path, String branch) throws BranchNotFoundException, IOException;

}
