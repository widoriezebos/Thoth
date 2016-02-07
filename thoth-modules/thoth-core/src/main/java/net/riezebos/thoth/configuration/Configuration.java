package net.riezebos.thoth.configuration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.exceptions.ContextNotFoundException;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;

public interface Configuration {

  public static final String BUILT_PROPERTIES = "net/riezebos/thoth/default.configuration.properties";
  public static final String WORKSPACELOCATION = "workspacelocation";
  public static final String REQUIRED_PREFIX = "net/riezebos/thoth/skins/";
  public static final String CLASSPATH_PREFIX = "classpath:";
  public static final String SKIN_PROPERTIES = "skin.properties";

  /**
   * Image recognition. Set (comma separated) the extensions (without the '.') below to determine whether a matching resource will be treated as an image
   * 
   * @return
   */
  String getImageExtensions();

  /**
   * The absolute path to the folder that will contain pulled contexts
   * @return
   */
  String getWorkspaceLocation();

  /**
   * The maximum time in ms that a Markdown parse can last
   * @return
   */
  long getParseTimeOut();

  
  /**
   * Returns the list of the names of all contexts defined
   * @return
   */
  List<String> getContexts();

  /**
   * Validates the configuration
   * @throws ConfigurationException when the configuration is invalid
   */
  void validate() throws ConfigurationException;

  /**
   * The extensions of the files that will be recognized as a 'Book' and therefore shown by the auto generated index
   * @return
   */
  List<String> getBookExtensions();

  /**
   * The extensions of the files that will be recognized as a 'Document' and therefore can be rendered to html, pdf etc
   * @return
   */
  List<String> getDocumentExtensions();

  /**
   * See https://github.com/sirthias/pegdown for a full description
   * @return
   */
  int getMarkdownOptions();

  /**
   * The URL for the localhost. Will be used for custom renderer processing
   * @return
   */
  String getLocalHostUrl();

  boolean appendErrors();

  int getFileMaxRevisions();

  int getEmbeddedServerPort();

  String getEmbeddedServerName();

  int getEmbeddedIdleTimeout();

  int getContextMaxRevisions();

  String getIndexExtensions();

  String getMainIndexSkinContext();

  String getDefaultSkin();

  Set<String> getContextIndexClassifications();

  boolean isPrettyPrintJson();

  List<CustomRendererDefinition> getCustomRenderers();

  SimpleDateFormat getDateFormat();

  int getMaxSearchResults();

  boolean isImageExtension(String extension);

  boolean isFragment(String path);

  boolean isBook(String path);

  boolean isResource(String path);

  String getValue(String key);

  String getValue(String key, String dflt);

  Map<String, ContextDefinition> getContextDefinitions();

  Map<String, RepositoryDefinition> getRepositoryDefinitions();

  ContextDefinition getContextDefinition(String name) throws ContextNotFoundException;
}
