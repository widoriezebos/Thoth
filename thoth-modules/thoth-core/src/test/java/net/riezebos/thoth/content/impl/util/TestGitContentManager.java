package net.riezebos.thoth.content.impl.util;

import net.riezebos.thoth.configuration.ContextDefinition;
import net.riezebos.thoth.configuration.ThothContext;
import net.riezebos.thoth.content.impl.GitContentManager;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.markdown.filehandle.ClasspathFileSystem;
import net.riezebos.thoth.markdown.filehandle.FileSystem;

public class TestGitContentManager extends GitContentManager {

  public TestGitContentManager(ContextDefinition contextDefinition, ThothContext context) throws ContentManagerException {
    super(contextDefinition, context);
  }

  @Override
  protected FileSystem createFileSystem() throws ContextNotFoundException {
    return new ClasspathFileSystem("non/existing/class/root/so/will/not/serve/anything");
  }

}
