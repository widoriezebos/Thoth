package net.riezebos.thoth.renderers;

import net.riezebos.thoth.configuration.ThothEnvironment;

public class TestRendererProvider implements RendererProvider {

  private ThothEnvironment thothEnvironment;

  public TestRendererProvider(ThothEnvironment thothEnvironment) {
    this.thothEnvironment = thothEnvironment;
  }

  @Override
  public Renderer getRenderer(String typeCode) {
    return new HtmlRenderer(thothEnvironment, this);
  }

}
