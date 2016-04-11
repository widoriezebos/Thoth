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
 */package net.riezebos.thoth.testutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.riezebos.thoth.content.comments.Comment;
import net.riezebos.thoth.content.comments.CommentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.util.ThothUtil;

public class TestCommentManager implements CommentManager {

  private List<Comment> comments = new ArrayList<>();
  long nextId = 1;

  @Override
  public Comment createComment(Comment comment) throws ContentManagerException {
    comments.add(comment);
    comment.setId(nextId++);
    Collections.sort(comments, (o1, o2) -> o1.getTimeCreated().compareTo(o2.getTimeCreated()));
    return comment;
  }

  @Override
  public boolean deleteComment(long id) throws ContentManagerException {
    Comment comment = getComment(id);
    if (comment != null)
      return comments.remove(comment);
    else
      return false;
  }

  @Override
  public boolean deleteComment(Comment comment) throws ContentManagerException {
    return deleteComment(comment.getId());
  }

  @Override
  public List<Comment> getComments(String contextName, String documentpath, String userName) throws ContentManagerException {
    String searchPath = ThothUtil.stripPrefix(documentpath, "/");
    return comments.stream() //
        .filter(c -> (searchPath == null || c.getDocumentPath().equals(searchPath)) //
            && (userName == null || c.getUserName().equals(userName)))//
        .collect(Collectors.toList());
  }

  @Override
  public Comment getComment(long id) throws ContentManagerException {
    Optional<Comment> comment = comments.stream().filter(c -> (c.getId() == id)).findFirst();
    if (comment.isPresent())
      return comment.get();
    else
      return null;
  }

  @Override
  public List<Comment> getOrphanedComments(String contextName, List<String> allPaths) throws ContentManagerException {
    List<Comment> result = new ArrayList<>();

    comments.stream().forEach(c -> {
      if (!allPaths.contains(c.getDocumentPath()))
        result.add(c);
    });

    return result;
  }

  @Override
  public Set<String> getReferencedPaths(String contextName) throws ContentManagerException {
    Set<String> result = new HashSet<>();
    comments.stream().forEach(c -> result.add(c.getDocumentPath()));
    return result;
  }

}
