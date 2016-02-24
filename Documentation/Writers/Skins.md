# Skinning Documents and Screens
Thoth comes equipped with built-in skins that enable you to change the appearance of screens and documents. Basically a Skin is a collection of templates, CSS and any other resource files required to render an HTML page in a certain way. By default the Skin named SimpleSkin will be chosen to render all screens and documents in Thoth. In the configuration you can change the default Skin, but it is also possible to influence the selection of the Sin in other ways

## Overriding the Skin dynamically
You can force Thoth to use a skin named ‘MySkin’ by adding a request parameter `skin=MySkin` 
An example forcing the (builtin) Skin named Bootstrap would be

	http://localhost:8080/skin=Bootstrap

## Overriding the Skin based on the path of a document (automatically)
In the root of your library you (should) have a plain text document called skins.properties and in this file you associate a Skin with a path. An example of this is 

	*/Datamodel/*=DataModelSkin
	*=SimpleSkin

In this example any path that matches \*/Datamodel/\* will be rendered using the DataModelSkin. As a fallback any other path will be associated with SimpleSkin. Matching is done from top to bottom and the first match wins.

## Creating your own Skin
Of course you can use the builtin Skins (SimpleSkin and Bootstrap) but you can define as many as you like yourself. You do this by creating a Skin descriptor file and placing this file anywhere in your library. Thoth will find all your Skin descriptors and enable them for use within your library.

### Creating the Skin descriptor file
The Skin descriptor file is a property file with the name `skin.properties`. Inside this file you must at least have one property that defines the name of the Skin. If you leave out everything else you have effectively defined a new Skin that inherits everything from SimpleSKin. So the minimal skin.properties file looks like 

	# The name of the skin. Used for referencing this skin in the configuration
	name=MyOwnSkin

Of course it does not make any sense to do this because now there is no difference compared to the SimpleSkin, so let’s have a look at a better example:

	# The name of the skin. Used for referencing this skin in the configuration
	name=MyOwnSkin
	
	# From which this Skin inherits anything not defined by this Skin itself
	inheritsfrom=
	
	# The velocity template that is used for the Main index (main page)
	template.index=index.tpl
	
	# The velocity template that is used for the Context index (context specific page)
	template.contextindex=contextindex.tpl
	
	# Template for Markdown rendered pages
	template.html=html.tpl
	
	# Template for Diff pages
	# Supports keyword replacement for ${title} and ${body} for the body of the page
	template.diff=diff.tpl
	
	# The velocity template that is used for the meta information page
	template.meta=meta.tpl
	
	# The velocity template that is used for the revision information page
	template.revisions=revisions.tpl
	
	# The velocity template that is used for the revision information page
	template.validationreport=validationreport.tpl
	
	# The velocity template that is used for the revision information page
	template.search=search.tpl
	
	# The velocity template that is used for browsing
	template.browse=browse.tpl
	
	# The velocity template that is used to display any error that occurred during execution
	template.error=error.tpl

Note the _inheritsfrom_ setting. If you leave it empty then you inherit from SimpleSkin. If you put a name of another (custom) Skin there, you will inherit from that Skin. This enables you to inherit from a base Skin without having to copy anything that you did not change.

The templates specified in the above example are Velocity templates that render the various pages and documents for Thoth. The paths of the templates are relative to the skin.properties file, it is good practice to place the files directly where the skin.properties file is located, and put all of them in a folder with the name of the Skin.

By changing these templates you can completely alter the way Thoth looks and feels. See the Skin Developers Manual for more information on how to create your own templates.

### Referencing Skin resources from your template
You can place any number of resources (css, images etc) inside the folder of your skin and then reference them from your template. To create fully qualified links there is the ${skinbase} variable to help you. See the example at the end of this chapter for more information (look for href="${skinbase}…”)

### JSON support
Any command in Thoth supports JSON as an output format; so every variable described below can be returned as part of a JSON encoded result as well (bypassing the template mechanism entirely). This can come in handy when your Skin uses AJAX to render a Single Page Interface for instance. If you want a command to return JSON then add a request parameter ‘`mode=json`’ to your request.

### Creating your own templates
Templates are written in [Velocity](http://velocity.apache.org) and can use variables provided by Thoth to include specific content. Depending on the command being rendered there will be different variables available (although some are generic).

#### Generic variables for all templates
The following variables are defined for all templates

- **context** is the name of the context. Type: String
- **skinbase** is the absolute path to the root of the active Skin’s resources. Type: String
- **contexturl** is absolute path to the current context. If Thoth is not located in the root of the web container then this path includes the path into the Thoth web context. Type: String
- **path** is the path part of the request inside the context (excluding context URL parts). Type: String
- **title** is the title of the current page or document. Type: String
- **skin** is the name of the active skin. Type: String
- **today** is the current date. Formatted according to the format mask specified by the Configuration. Type: String
- **now** is the current timestamp. Formatted according to the format mask specified by the Configuration. Type: String
- **refresh** is the timestamp of the latest successful refresh (for version control based repositories). Formatted according to the format mask specified by the Configuration.  Type: String
- **outputFormats** is the list of supported output formats (html, raw and any custom renderers that are defined in the configuration) Type: List\<String\>

#### Types used for variables
Most types are basic / simple types like String, List and Integer. The exception to the rule are beans Book, Diff etc. The following beans are defined as types and used below:

##### Book
> String **name**
> String **path**
> String **folder**
> String **title**
> Map\<String, String\> **metaTags**

##### Diff
> Operation **operation** (One of INSERT, DELETE or EQUAL)
> String **text**

##### MarkDownDocument
> Map\<String, String\> **metatags**
> List\<ProcessorError\> **errors**
> DocumentNode **documentStructure**
> String **markdown**
> Date **lastModified**

##### DocumentNode
> String **path**
> String **description**
> String **fileName**
> String **folder**
> int **includePosition**
> int **level**
> List\<DocumentNode\> **children**

##### Commit
> String **id**
> String **author**
> Date **timestamp**
> String **message**
> String **shortMessage**
> List\<Revision\> **revisions**

##### ProcessorError
> LineInfo **currentLineInfo**
> String **errorMessage**

##### LineInfo
> String **file**
> int **line**

##### SearchResult
> String **document**
> List\<Fragment\> **fragments**
> List\<DocumentNode\> **bookReferences**
> boolean **isResource**
> boolean **isImage**
> int **indexNumber**

##### Fragment
> String **text**

##### ContentNode
> String **path**
> boolean **isFolder**
> Date **dateModified**
> long **size**
> List\<ContentNode\> **children**

#### template.index
The main index template is a special one, because the template itself belongs to a specific repository (context) by it is used for the main index that is not specific to a certain context. The following variables are available:

- **contexts** is the list of contexts that are available. Type: String

#### template.contextindex
The index of the context; this is where you would list all the books that are present in 

- **classifications** contains the list of names for the classifications specified in the configuration. For every classification name there will be list of books that are marked to be part of that classification using meta data (i.e. classification: \<name\>) Type: List\<String\>
- **classification\_\<classification\>** contains the list of books per classification as defined in the configuration (see the setting context.classifications under [File Classification](../Administration/Configuration.md#fileclassification)). Type: List\<Book\>
- **books** contains a list of all books sorted by path Type: List\<Book\>
- **versioncontrolled** is a boolean that is true when the underlying repository supports version control. Type: boolean

#### template.html
The template used to render HTML pages.

- **body** is the actual (rendered) contents of the document. In the case of HTML this will be the HTML without any header (just what you would place inside the \<body\> tag). Type: String

#### template.diff
The template used to display the revisions of a document (fragment).

- **author** is the author of the revision; based on information from the version control system. Type: String
- **timestamp** is the timestamp of the committed revision. Formatted according to the format mask specified by the Configuration. Type: String
- **commitMessage** is the message that the author entered for the commit. Type: String
- **diffs** is the list of Diff’s (revisions) in time. Type: List\<Diff\>

#### template.meta
The template used to display meta information of a document.

- **document** is the document concerned. Type: MarkDownDocument
- **usedBy** is the list of document (fragment) paths that directly reference or include the document concerned. Type: List\<String\>
- **usedByIndirect** is the list of document (fragment) paths that directly OR indirectly reference or include the document concerned. Type: List\<String\>
- **documentNodes** is the list of DocumentNodes that make up the document. Type: List\<DocumentNode\>
- **commitMap** is the list of commits per document path. Type: Map\<String, List\<Commit\>\> 
- **commitList** is the list of commits for the document concerned. Type: List\<Commit\>
- **metatagKeys** is the list of meta tag names (keys) for the document concerned. Type: List\<String\> 
- **metatags** is the map of for metatag key-value pairs. Use metatagKeys to iterate over the map. Type: Map\<String, String\> 
- **errors** is the list of errors that occurred during (include) processing of the document concerned. Type: List\<ProcessorError\>
- **versioncontrolled** returns true if the underlying repository supports version control. Type: Boolean

#### template.revisions
The template that is used to display all the revisions of changes committed to the (version controlled) repository.

- **commitList** is the list of commits. Type: List\<Commit\>
- **page** is the current page. Note that paging is supported by adding a request parameter called ‘page’ with the value of the page to be displayed. Type: Integer
- **hasmore** is a boolean that will be true if there are results to be found on the next page. Type: Boolean

#### template.validationreport
The page that shows all errors that were detected by Thoth in the current repository.

- **errors** is the list of errors that occurred during (include) processing of the document concerned. Type: List\<ProcessorError\>

#### template.search
The search (engine) page, displaying the results of a query.

- **query** is the query that was entered. Type: String
- **searchResults** contains the search results. Type: List\<SearchResult\>
- **errorMessage** contains any error message in the case of a search query problem (syntax problem while parsing the Lucene query). Type: String
- **page** is the current page. Note that paging is supported by adding a request parameter called ‘page’ with the value of the page to be displayed. Type: Integer
- **hasmore** is a boolean that will be true if there are results to be found on the next page. Type: Boolean

#### template.browse
The template used for browsing the contents of the entire repository.

- **contentNodes** is the list of content nodes for the current folder (path). Type: List\<ContentNode\>
- **atRoot** is a boolean that will be true if the current page is representing the root of the repository

#### template.error
The template used to display Thoth system errors.

- **message** is the (technical) error message. Type: String
- **stack** is the full stack trace of the error occurring in Thoth. Type: String

#### Template helper functions
There is a special variable called _thothutil_ that you can use to access all the functions defined by the ThothUtil helper class. The following functions are available:

String escapeHtml(String html)
: Escapes the characters in a String using HTML entities.

String formatTimestamp(Date date)
: Formats a Timestamp (a Date actually) according to the configured format mask for timestamps

String formatDate(Date date)
: Formats a Date according to the configured format mask for dates.

String getVersion()
: Returns the current version of Thoth

String encodeUrl(String url)
: Translates a string into application/x-www-form-urlencoded format using a specific encoding scheme. This method uses the supplied encoding scheme to obtain the bytes for unsafe characters.

String getCanonicalPath(String path)
: Returns the canonical path (translating relative constructs like ‘..’ and ‘.’)

String getNameOnly(String imagePath) 
: Returns just the name (stripping the path and prefix) of a filename

String getFileName(String filespec)
: Returns just the filename (stripping any directories from the path)

String getFolder(String filespec)
: Returns the folder part of a fully specified filename.

String stripSuffix(String value, String suffix)
: Strips the given suffix from the value string (if present)

String getPartBeforeFirst(String value, String prefix)
: Returns the part of the string value before the first prefix. If not found the entire string value

String getPartBeforeLast(String value, String prefix)
: Returns the part of the string value before the last prefix. If not found the entire string value

String getPartAfterFirst(String value, String prefix)
: Returns the part of the string value after the first prefix. If not found the entire string value

String getPartAfterLast(String value, String prefix)
: Returns the part of the string value after the last prefix. If not found the entire string value

String getExtension(String path)
: Returns the extension part of a given filename (with or without the path)

String prefix(String value, String prefix)
: Prefixes value with the given string. If the prefix is already there nothing is added

String suffix(String value, String prefix)
: Suffixes value with the given string. If the prefix is already there nothing is added

#### Template example
The template below is a simple example; it is actually the template used by SimpleSkin to render the main index page. It should give you a feel of what a template looks like though. Especially note the _\#foreach_, _${thothutil.function()}_ and _${variable}_ constructs

\includecode{index.tpl}
