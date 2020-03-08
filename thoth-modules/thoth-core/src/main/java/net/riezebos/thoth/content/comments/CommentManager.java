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
package net.riezebos.thoth.content.comments;

import java.util.List;
import java.util.Set;

import net.riezebos.thoth.exceptions.ContentManagerException;

public interface CommentManager {

  Comment createComment(Comment comment) throws ContentManagerException;

  boolean deleteComment(long id) throws ContentManagerException;

  boolean deleteComment(Comment comment) throws ContentManagerException;

  List<Comment> getComments(String contextName, String documentpath, String userName) throws ContentManagerException;

  Comment getComment(long id) throws ContentManagerException;

  List<Comment> getOrphanedComments(String contextName, List<String> allPaths) throws ContentManagerException;

  Set<String> getReferencedPaths(String contextName) throws ContentManagerException;

}
