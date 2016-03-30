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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.beans.Book;
import net.riezebos.thoth.beans.ContentNode;
import net.riezebos.thoth.beans.MarkDownDocument;
import net.riezebos.thoth.configuration.CacheManager;
import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.content.skinning.SkinManager;
import net.riezebos.thoth.content.versioncontrol.Commit;
import net.riezebos.thoth.content.versioncontrol.SourceDiff;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.exceptions.SkinManagerException;
import net.riezebos.thoth.markdown.critics.CriticProcessingMode;
import net.riezebos.thoth.markdown.filehandle.FileHandle;
import net.riezebos.thoth.markdown.util.ProcessorError;
import net.riezebos.thoth.util.PagedList;

public interface ContentManager {

  public String NATIVERESOURCES = "/nativeresources";

  String refresh() throws ContentManagerException;

  void reindex();

  MarkDownDocument getMarkDownDocument(String documentPath, boolean suppressErrors, CriticProcessingMode criticProcessingMode)
      throws IOException, ContextNotFoundException;

  List<Book> getBooks() throws ContextNotFoundException, IOException;

  PagedList<Commit> getCommits(String path, int pageNumber, int pageSize) throws ContentManagerException;

  public SourceDiff getDiff(String id) throws ContentManagerException;

  List<ContentNode> list(String path) throws ContextNotFoundException, IOException;

  public List<ContentNode> find(String fileSpec, boolean recursive) throws IOException;

  List<ContentNode> getUnusedFragments() throws IOException, ContentManagerException;

  Date getLatestRefresh();

  void enableAutoRefresh() throws ContentManagerException;

  void disableAutoRefresh();

  String getContextName();

  String getIndexFolder() throws ContextNotFoundException;

  String getReverseIndexFileName() throws ContextNotFoundException;

  public String getReverseIndexIndirectFileName() throws ContextNotFoundException;

  String getErrorFileName() throws ContextNotFoundException;

  long getContextChecksum() throws IOException, ContextNotFoundException;

  boolean supportsVersionControl();

  FileHandle getFileHandle(String contextFile);

  SkinManager getSkinManager() throws SkinManagerException;

  InputStream getInputStream(String path) throws IOException;

  Configuration getConfiguration();

  boolean isFragment(String string);

  List<ProcessorError> getValidationErrors() throws ContentManagerException;

  Map<String, List<String>> getReverseIndex(boolean indirect) throws ContentManagerException;

  CacheManager getCacheManager();

  void expireCache();

  String getLibraryRoot();

  AccessManager getAccessManager();

}
