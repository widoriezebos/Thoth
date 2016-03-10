package net.riezebos.thoth.configuration;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import net.riezebos.thoth.context.ContextDefinition;
import net.riezebos.thoth.context.RepositoryDefinition;
import net.riezebos.thoth.exceptions.ConfigurationException;
import net.riezebos.thoth.renderers.util.CustomRendererDefinition;

public interface Configuration extends Cloneable {

  public static final String DEFAULT_TIMESTAMP_FMT = "dd-MM-yyyy HH:mm:ss";

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
   * 
   * @return
   */
  String getWorkspaceLocation();

  /**
   * The maximum time in ms that a Markdown parse can last
   * 
   * @return
   */
  long getParseTimeOut();

  /**
   * Validates the configuration
   * 
   * @throws ConfigurationException when the configuration is invalid
   */
  void validate() throws ConfigurationException;

  /**
   * The extensions of the files that will be recognized as a 'Book' and therefore shown by the auto generated index
   * 
   * @return
   */
  List<String> getBookExtensions();

  /**
   * The extensions of the files that will be recognized as a 'Document' and therefore can be rendered to html, pdf etc
   * 
   * @return
   */
  List<String> getDocumentExtensions();

  /**
   * See https://github.com/sirthias/pegdown for a full description
   * 
   * @return
   */
  int getMarkdownOptions();

  /**
   * The URL for the localhost. Will be used for custom renderer processing
   * 
   * @return
   */
  String getLocalHostUrl();

  /**
   * Auto number headings up to the specified level. Default is 3, set to 0 to disable
   * 
   * @return
   */
  int getMaxHeaderNumberingLevel();

  /**
   * Append any link / include error messages at the bottom of the document
   * 
   * @return
   */
  boolean appendErrors();

  /**
   * The maximum number of revisions to collect / display for latest commits per file (Meta command)
   * 
   * @return
   */
  int getFileMaxRevisions();

  /**
   * The port to have the (embedded) server listen to (when running standalone). Default is 8080
   * 
   * @return
   */
  int getEmbeddedServerPort();

  /**
   * The name of the host (when running standalone). Default is localhost
   * 
   * @return
   */
  String getEmbeddedServerName();

  /**
   * The idle timeout for connections. Note: in specified in seconds
   * 
   * @return
   */
  int getEmbeddedIdleTimeout();

  /**
   * The maximum number of revisions to collect / display for latest commits for the entire context (Revisions command)
   * 
   * @return
   */
  int getContextMaxRevisions();

  /**
   * The extensions of file to include in the search index (comma separated; no '.')
   * 
   * @return
   */
  String getIndexExtensions();

  /**
   * # The skin to use for the main index page (hence not within a context). Must be a valid context name as specified by the 'context.name' property. If left
   * blank then the default skin is used.
   * 
   * @return
   */
  String getMainIndexSkinContext();

  /**
   * The name of the default skin to use. If not set; it will use the builtin skin named 'Builtin' Note that any skin can come from the classpath as long as the
   * package remains within net/riezebos/thoth/skins/
   * 
   * @return
   */
  String getDefaultSkin();

  /**
   * The following classifications will be available to the context index template (grouping of documents by classification) The folder classification is built
   * in; and is just listed below for clarity In the template (or json) the classification names are available under "classification_" + <the name specified
   * below>
   */
  List<String> getContextIndexClassifications();

  /**
   * Pretty prent JSON responses. You might want to set this to false in a production environment; small performance benefit
   * 
   * @return
   */
  boolean isPrettyPrintJson();

  /**
   * @return the list of defined CustomRenderers
   */
  List<CustomRendererDefinition> getCustomRenderers();

  /**
   * The date format including the time part mask to use for Thoth pages
   * 
   * @return
   */
  SimpleDateFormat getTimestampFormat();

  /**
   * The date format excluding the time part mask to use for Thoth pages
   * 
   * @return
   */
  SimpleDateFormat getDateFormat();

  /**
   * The maximum number of search results to display per page of results
   * 
   * @return
   */
  int getMaxSearchResults();

  /**
   * Returns true for extensions considered to be for images
   * 
   * @param extension
   * @return
   */
  boolean isImageExtension(String extension);

  /**
   * Returns true for extensions considered to be for fragments (markdown documents)
   * 
   * @param extension
   * @return
   */
  boolean isFragment(String path);

  /**
   * Returns true for extensions considered to be for books (markdown books)
   * 
   * @param extension
   * @return
   */
  boolean isBook(String path);

  /**
   * Returns true for anything that is considered a resource (not markdown)
   * 
   * @param extension
   * @return
   */
  boolean isResource(String path);

  /**
   * Return the value of a property. Will log an error is returns null
   * 
   * @param key
   * @return
   */
  String getValue(String key);

  /**
   * Return the value of a property. Will return dflt when the value is null
   * 
   * @param key
   * @return
   */
  String getValue(String key, String dflt);

  /**
   * Returns a map of all defined Contexts. The key is the name (in lowercase) of the context.
   * 
   * @return
   */
  Map<String, ContextDefinition> getConfiguredContextDefinitions();

  /**
   * Returns a map of all defined Repositories. The key is the name (in lowercase) of the repository.
   * 
   * @return
   */
  Map<String, RepositoryDefinition> getConfiguredRepositoryDefinitions();

  /**
   * @return a list of all supported output formats, including the custom renderers that are configured
   */
  List<String> getOutputFormats();

  /**
   * Reloads the configuration, supporting hot configuration changes
   * 
   * @throws FileNotFoundException
   * @throws ConfigurationException
   */
  void reload() throws FileNotFoundException, ConfigurationException;

  /**
   * For file based configurations: return the path of the property file. For other configurations this will probably be just an informative name.
   * 
   * @return
   */
  public String getPropertyFileName();

  /**
   * @return a clone of the configuration
   */
  public Configuration clone();

  /**
   * Automatically add a newline for every header in the source file. Default is true
   * 
   * @return
   */
  boolean addNewlineBeforeheader();

  /**
   * Notifies that this configuration is to be discarded; so release any resources or detach any listeners
   */
  void discard();

  /**
   * When true configuration changes will be detected and will cause an automatic reload of the configuration to occur. Default is true
   * 
   * @return
   */
  boolean isAutoReload();

  /**
   * @return The interval in seconds to look for changes to the configuration (based on the timestamp of the configuration file)
   */
  int getAutoReloadInterval();

  /**
   * Returns the name of the group for the anonymous user.
   * 
   * @return
   */
  String getDefaultGroup();

  /**
   * Returns the type of database to use for persistent storage. Use 'embedded' if you want to use the built-in database. Supported types are 'embedded',
   * 'oracle' and 'postgres' although others might work depending on the URL used.
   * 
   * @return
   */
  public String getDatabaseType();

  /**
   * Returns the JDBC URL of database to use for persistent storage. When the database type is set to embedded then just enter the path (directory) where you
   * want the database files to be created/stored. When using other database types make sure the driver is on the classpath so that the DriverManager can find
   * the correct JDBC driver.
   * 
   * @return
   */
  public String getDatabaseUrl();

  /**
   * Returns the database user for persistent storage
   * 
   * @return
   */
  public String getDatabaseUser();

  /**
   * Returns the database password for persistent storage
   * 
   * @return
   */
  public String getDatabasePassword();

  /**
   * The master password to encrypt all stored passwords with. NOTE! If you change the master password all passwords that are stored in the database will become
   * invalid!
   * 
   * @return
   */
  public String getPasswordEncryptionKey();

  /**
   * The name of the server, used in screens (titles). Available in the templates using $servername
   * 
   * @return
   */
  public String getServerName();

}
