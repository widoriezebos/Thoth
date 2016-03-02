package net.riezebos.thoth.commands.util;

import java.util.ArrayList;

import net.riezebos.thoth.commands.SearchCommand;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.content.search.SearchResult;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.SearchException;
import net.riezebos.thoth.renderers.RendererProvider;
import net.riezebos.thoth.user.Identity;
import net.riezebos.thoth.util.PagedList;

public class TestSearchCommand extends SearchCommand {

  public TestSearchCommand(ThothEnvironment thothEnvironment, RendererProvider rendererProvider) {
    super(thothEnvironment, rendererProvider);
  }

  @Override
  protected PagedList<SearchResult> search(Identity identity, String context, String query, Integer pageNumber, int pageSize)
      throws ContentManagerException, SearchException {
    return new PagedList<SearchResult>(new ArrayList<>(), false);
  }
}
