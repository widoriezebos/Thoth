package net.riezebos.thoth.renderers;

import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.testutil.ThothTestBase;

public class RendererTest extends ThothTestBase implements RendererProvider {

  @Override
  public Renderer getRenderer(String typeCode) {
    try {
      return new HtmlRenderer(createThothContext("dummy"), this);
    } catch (ContentManagerException e) {
      throw new IllegalArgumentException(e);
    }
  }

}
