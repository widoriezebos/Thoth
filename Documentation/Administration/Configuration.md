# The configuration file
In this section you will find the settings Thoth supports. The configuration of Thoth is placed in a property file which is read during startup. Without the minimal configuration (which is about where to store working files and where to find content) Thoth will not be able to start

## Required settings
workspacelocation
: States where the working files of Thoth will be created. This is where Thoth will checkout branches and create search indexes. In principle it is completely safe to delete the entire contents of the workspace (after shutting down Thoth). When Thoth is launched it will automatically recreate the contents of the workspace, including any embedded database. But NOTE: If you delete the ‘thoth-database’ folder in your workspace you will *also have deleted the embedded Thoth database*. Although the embedded database will be recreated automatically on startup, you will have lost all your users and groups as a result.

### defaultgroup
: When not logged in; this group defines the (default) permissions. By default the following groups are defined by Thoth: thoth\_administrators, thoth\_writers, thoth\_readers, thoth\_anonymous. Administrators can do anything in Thoth, writers can not pull nor reindex nor do user management but basically anything else. Readers can only access books and anonymous has only access to the main index page (so they can login from there)

### Repository settings
Repositories are the source for content. In the case of a Git repository Thoth will pull a branch as the source of the content. In the case of a FileSystem repository nothing will be pulled; the specified location for the repository is then used as-is.

repository.1.name
: The uniquely identifying name of a repository. A repository is currently a Git or a local folder; future versions of Thoth might support additional types of repositories. The name is used as a reference when you specify a context (see below). Note the numeric index in the name (1) which enumerates the repository definition. You can define as many repositories as long as you number them sequentially leaving no gaps. Thoth will stop at the first repository.*n*.name that does not have it’s value set.

repository.1.type
: The type of repository. Choose one of **git**, **filesystem**, **zip**. You can use the alias ‘fs’ for filesystem and ‘jar’ for zip.

repository.1.location
: The location of the repository. For a Git repository this will be the URL, for a FileSystem repository it is the (absolute) folder name on the filesystem and for a Zip it is the location of the zip (or jar) file. Note that for zips you can suffix the path with ‘!/pathinzip’ to offset the contents of the zip. So if your zip contains a root folder called Documentation (and you want that folder to serve as root inside the zip) you would have something like `location=/path/to/myzip.zip!/Documentation`

repository.1.username
: The username for logging in to the repository. In the case of a FS or Zip repository this can be left blank.

repository.1.password
: The password for logging in to the repository. In the case of a FS or Zip repository this can be left blank.

### Database settings
Thoth uses a database to store it’s users and groups (among others). If you use the defaults ‘as-is’ then the database will be a Derby embedded one that needs no additional setup (will be automatically created on startup, default location will be a subfolder ‘thoth-database’ in your workspace. If you require a different database, or if you want your derby embedded database files located outside your workspace, then you need to set the properties below. Also note that you need to add the appropriate JDBC driver jar on the classpath. The tables will be automatically created (and filled with initial values) regardless the type of database.

database.type
: The type of database (driver) to use. Supported are ‘embedded’, ‘derby’, ’oracle’ and ‘postgresql’. Unsupported but available are also ‘h2’, ‘hsqldb’ and ‘mysql’. The default is ‘embedded’.

database.url
: The JDBC url for the database. If you use the embedded database this can be just a folder relative to your workspace or an absolute folder if you want the database files to be stored outside your workspace. In all other cases the JDBC url depends on the driver chosen. For Postgresql you would have something like jdbc:postgresql://localhost:5432/thoth

database.user
: The username to connect with to the database. Can leave empty for embedded.

database.password
: The password to connect with to the database. Can leave empty for embedded.

### Context settings
A context if the root of a library, and corresponds to a branch in Git. For FileSystem  repositories there is not branch, but the name of the context is used in Thoth to refer to the contents. Note the numeric index in the name (1) which enumerates the context definition. You can define as many contexts as long as you number them sequentially leaving no gaps. Thoth will stop at the first context.*n*.name that does not have it’s value set.

context.1.name
: The uniquely identifying name of a context. This name will be part of any path to a document inside the context; and the main index page of Thoth by default displays a list of known contexts.

context.1.repository
: The reference (by name) to the repository where the contents of this context resides.

context.1.branch
: The name of the branch to pull from Git. In the case of FS repository this can be left blank.

context.1.library
: Optional. If you do not have your documentation (your library) located at the root of the repository, you can specify the location of your library as a relative path from the root of your repository. For example, in the case of the Thoth documentation the library is set to ‘Documentation’

context.1.refreshseconds
: The number of seconds between automatic repository refreshes. When a change is detected an indexer updating the search index is automatically launched. Default = 60

## Optional settings

defaultuser
: The name of the default user. Any user of Thoth will fallback to this user since there is no user management yet. In a future version of Thoth there will be support for users, groups and permissions. Right now the only way to restrict access to Thoth is to set the value of this property to one of “reader”, “writer”, “administrator”. Readers can only read books and search books. Writers have full access except for administrative functions like Pull and Reindex. Note: the default value is currently “administrator”; this property will no doubt be removed in a next release of Thoth.

config.autorefresh
: When true the configuration will automatically (hot) reload if changes to it are detected (based on comparing file modification timestamps)

config.autorefreshinterval
: The interval (in seconds) to check whether if the configuration has changed.

skin.default
: The name of the default skin. When no skin is defined for a specific context this is the skin that will be used. Also used when no skin is defined for the main index page. Default value is ‘SimpleSkin’

skin.mainindexcontext
: Use the skin associated with the given context for the main index page. If left blank the skin.default property will be used as a fallback. Default value is \<no value\>

localhost
: Custom rendering uses a forked process to render the contents. When this process needs to fetch the contents; this URL is used as the base for the path of the document. Default value is ‘http://localhost:8080/'

### Custom renderers
You can define any number of custom renderers. The most basic one would be a PDF or an EPUB renderer.

renderer.1.extension
: The extension to recognize this renderer. An example would be ‘pdf’, adding a parameter to a Thoth page URL ‘?output=pdf’ would then render the HTML version of that page with this renderer. Must be unique across renderers. Example: ‘pdf’

renderer.1.source
: The type of input to produce for the custom renderer. Default is HTML but you can override this to RAW (or any other renderer for that matter, just use it’s Extension as the source attribute). You obviously need to avoid setting up recursive loops by pointing to a custom renderer that uses this renderer as a source somewhere in it’s chain.

renderer.1.contenttype
: The mime contenttype of the content rendered by this custom renderer. For PDF that would be ‘application/pdf’

renderer.1.command
: The OS level command to render the output. The following keywords can be used for substitution in the command:

- input, the file that contains the source document
- url, the URL where to fetch the contents of the source document from. Note that this is an alternative method of specifying the input (without using the input file directly)
- output, the name of the file to write the rendered contents to
- context, the name of the context
- path, the path of the file in the context
- title, the title of the document

An few examples of custom renderers (note the use of ${url} and ${input}):

	renderer.1.extension=pdf 
	renderer.1.source=html 
	renderer.1.contenttype=application/pdf 
	renderer.1.command=/usr/local/bin/prince ${url}?suppresserrors=true -o ${output} --javascript --media=print --page-size=A4 --page-margin=20mm 
	
	renderer.2.extension=docx
	renderer.2.source=html 
	renderer.2.contenttype=application/vnd.openxmlformats-officedocument.wordprocessingml.document 
	renderer.2.command=/usr/local/bin/pandoc -s -r html -t docx ${input} -o ${output}
	
	renderer.3.extension=epub
	renderer.3.source=html 
	renderer.3.contenttype=application/epub+zip
	renderer.3.command=/usr/local/bin/pandoc -s -r html -t epub ${input} -o ${output}
	
	renderer.4.extension=rtf
	renderer.4.source=html 
	renderer.4.contenttype=application/rtf
	renderer.4.command=/usr/local/bin/pandoc -s -r html -t rtf ${input} -o ${output}

Note: on Windows the command for Prince is different:

	renderer.1.command=/Program Files/Prince/engine/bin/prince.exe ${url}?suppresserrors=true -o ${output} --javascript --media=print --page-size=A4 --page-margin=20mm 

And for Pandoc it will be like (note the user name in the path):

	renderer.2.command=C:/Users/Wido/AppData/Local/Pandoc/pandoc.exe -s -r html -t docx ${input} -o ${output} --variable=geometry:a4paper

### Using Pandoc as the HTML rendering engine
Note that it is possible to replace the default Markdown rendering engine (based on PegDown) with any other. Just define a custom renderer with the extension ‘html’ to override the built in HTML renderer and make sure you use ‘raw’ as the source input like so:

	renderer.1.extension=html
	renderer.1.contenttype=text/html
	renderer.1.source=raw
	renderer.1.command=/usr/local/bin/pandoc -s -r markdown -t html ${input} -o ${output} 

### Format masks
formatmask.timestamp
: The format mask to use for timestamps. Note that this is a Java based format mask (i.e. MM is for month, mm for minutes. Check [SimpleDateFormatter](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) for more information about this mask). Default value = dd-MM-yyyy HH:mm:ss

formatmask.date
: The format mask to use for dates (without the time part). Note that this is a Java based format mask (i.e. MM is for month, mm for minutes. Check [SimpleDateFormatter](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) for more information about this mask). Default value = dd-MM-yyyy

### File classification
context.classifications
: Comma separated list of classifications. The default branch index page uses the classifications of the documents to create an initial structure. There must be meta tags in the documents matching the classification (i.e. category: primers) for a classification named ‘category’. Default value = category,audience,folder

documents
: The extensions of the files that will be recognized as a 'Document' and therefore can be rendered to html, pdf etc. Default = marked,book,index,md

books
: The extensions of the files that will be recognized as a 'Book' and therefore shown by the auto generated index. Default = marked,book,index

index.extensions
: The extensions of file to include in the search index (comma separated; no ‘.’). Default = md,book,marked,txt

images.extensions
: Image recognition. Set (comma separated) the extensions (without the '.') below to determine whether a matching resource will be treated as an image. Default = png,jpeg,jpg,gif,tiff,bmp

### Markdown processing options

markdown.newlineheaders
: Will add a newline before every header in the source. Avoids some parser issues where strictness is involved. Default = on

markdown.appenderrors
: Append any link / include error messages at the bottom of the document. Default = on

markdown.option.SMARTS
: Pretty ellipses, dashes and apostrophes. Default = on

markdown.option.QUOTES
: Pretty single and double quotes. Default = on

markdown.option.ABBREVIATIONS
: PHP Markdown Extra style abbreviations. See [http://michelf.com/projects/php-markdown/extra/#abbr](http://michelf.com/projects/php-markdown/extra/#abbr). Default = on

markdown.option.HARDWRAPS
: Enables the parsing of hard wraps as HTML linebreaks. Similar to what github does. See [http://github.github.com/github-flavored-markdown](http://github.github.com/github-flavored-markdown). Default = on

markdown.option.AUTOLINKS
: Enables plain autolinks the way github flavoured markdown implements them. With this extension enabled pegdown will intelligently recognize URLs and email addresses without any further delimiters and mark them as the respective link type. See [http://github.github.com/github-flavored-markdown](http://github.github.com/github-flavored-markdown). Default = off

markdown.option.TABLES
: Table support similar to what Multimarkdown offers. See [http://fletcherpenney.net/multimarkdown/users\_guide/](http://fletcherpenney.net/multimarkdown/users_guide/). Default = on

markdown.option.DEFINITIONS
: PHP Markdown Extra style definition lists. Additionally supports the small extension proposed in the article referenced below. See [http://michelf.com/projects/php-markdown/extra/#def-list](http://michelf.com/projects/php-markdown/extra/#def-list) and [http://www.justatheory.com/computers/markup/modest-markdown-proposal.html](http://www.justatheory.com/computers/markup/modest-markdown-proposal.html) Default = on

markdown.option.FENCED\_CODE\_BLOCKS
: PHP Markdown Extra style fenced code blocks. See [http://michelf.com/projects/php-markdown/extra/#fenced-code-blocks](http://michelf.com/projects/php-markdown/extra/#fenced-code-blocks). Default = on

markdown.option.WIKILINKS
: Support Wiki Style links. See [http://github.github.com/github-flavored-markdown](http://github.github.com/github-flavored-markdown). Default = on

markdown.option.STRIKETHROUGH
: Support strikethroughs as supported in Pandoc and Github. Default = on

markdown.option.ANCHORLINKS
: Enables anchor links in headers. Note that Thoth has it’s own method of creating actor links that might interfere with this setting. Default = off

markdown.option.SUPPRESS\_HTML\_BLOCKS
: Suppresses HTML blocks. They will be accepted in the input but not be contained in the output. Default = off

markdown.option.SUPPRESS\_INLINE\_HTML
: Suppresses inline HTML tags. They will be accepted in the input but not be contained in the output. Default = off

markdown.option.ATXHEADERSPACE
: Requires a space char after Atx # header prefixes, so that #dasdsdaf is not a header. Default = off

markdown.option.FORCELISTITEMPARA
: Force List and Definition Paragraph wrapping if it includes more than just a single paragraph. Default = off

markdown.option.RELAXEDHRULES
: Force List and Definition Paragraph wrapping if it includes more than just a single paragraph: Default = off

markdown.option.TASKLISTITEMS
: GitHub style task list items. Default = on

markdown.option.EXTANCHORLINKS
: Generate anchor links for headers using complete contents of the header. Note that Thoth has it’s own method of creating actor links that might interfere with this setting. Default is off. 

### Miscellaneous
search.maxresults
: The number of search results to show per page of results

versioncontrol.maxfilerevisions
: The maximum number of revisions to retrieve for the metadata page. Default = 10

versioncontrol.maxcontextrevisions
: The maximum number of revisions to collect / display for latest commits for the entire context (Revisions command). Default = 25

json.prettyprint
: When a page is rendered to JSON (by adding the mode=json parameter to a request) it will be pretty printed if this setting is set to ‘true’. Default = true

parsetimeout
: The maximum time in ms that a Markdown parse by the PegDown parser can last. Default = 4000

## Sample configuration file
\includecode{sample.configuration.properties}