# The configuration file
In this section you will find the settings Thoth supports. The configuration of Thoth is placed in a property file which is read during startup. Without the minimal configuration (which is about where to store working files and where to find content) Thoth will not be able to start

## Required settings
workspacelocation
: States where the working files of Thoth will be created. This is where Thoth will checkout branches and create search indexes. In principle it is completely safe to delete the entire contents of the workspace (after shutting down Thoth). When Thoth is launched it will automatically recreate the contents of the workspace.

### Repository settings
Repositories are the source for content. In the case of a Git repository Thoth will pull a branch as the source of the content. In the case of a FileSystem repository nothing will be pulled; the specified location for the repository is then used as-is.

repository.1.name
: The uniquely identifying name of a repository. A repository is currently a Git or a local folder; future versions of Thoth might support additional types of repositories. The name is used as a reference when you specify a context (see below). Note the numeric index in the name (1) which enumerates the repository definition. You can define as many repositories as long as you number them sequentially leaving no gaps. Thoth will stop at the first repository.*n*.name that does not have it’s value set.

repository.1.type
: The type of repository. Currently ‘git’ and ‘fs’ are supported.

repository.1.location
: The location of the repository. For a Git repository this will be the URL and for a FileSystem repository it is the (absolute) folder name on the filesystem.

repository.1.username
: The username for logging in to the repository. In the case of FS repository this can be left blank.

repository.1.password
: The password for logging in to the repository. In the case of FS repository this can be left blank.

### Context settings
A context if the root of a library, and corresponds to a branch in Git. For FileSystem  repositories there is not branch, but the name of the context is used in Thoth to refer to the contents. Note the numeric index in the name (1) which enumerates the context definition. You can define as many contexts as long as you number them sequentially leaving no gaps. Thoth will stop at the first context.*n*.name that does not have it’s value set.

context.1.name
: The uniquely identifying name of a context. This name will be part of any path to a document inside the context; and the main index page of Thoth by default displays a list of known contexts.

context.1.repository
: The reference (by name) to the repository where the contents of this context resides.

context.1.branch
: The name of the branch to pull from Git. In the case of FS repository this can be left blank.

context.1.refreshseconds
: The number of seconds between automatic repository refreshes. When a change is detected an indexer updating the search index is automatically launched. Default = 60

## Optional settings
skin.default
: The name of the default skin. When no skin is defined for a specific context this is the skin that will be used. Also used when no skin is defined for the main index page. Default value is ‘SimpleSkin’

skin.mainindexcontext
: Use the skin associated with the given context for the main index page. If left blank the skin.default property will be used as a fallback. Default value is \<no value\>

localhost
: Custom rendering uses a forked process to render the contents. When this process needs to fetch the contents; this URL is used as the base for the path of the document. Default value is ‘http://localhost:8080/'

### Custom renderers
You can define any number of custom renderers. The most basic one would be a PDF renderer.

renderer.1.extension
: The extension to recognize this renderer. An example would be ‘pdf’, adding a parameter to a Thoth page URL ‘?output=pdf’ would then render the HTML version of that page with this renderer. Must be unique across renderers. Example: ‘pdf’

renderer.1.contenttype
: The mime contenntype of the content rendered by this custom renderer. For PDF that would be ‘application/pdf’

renderer.1.command
: The OS level command to render the output. The following keywords can be used for substitution in the command:

- url, the URL where to fetch the contents from
- output, the name of the file to write the rendered contents to
- context, the name of the context
- path, the path of the file in the context
- title, the title of the document

An example of a command:

	/usr/local/bin/prince ${url} -o ${output} --media=print --page-size=A4 --page-margin=20mm

context.classifications
: Comma separated list of classifications. The default branch index page uses the classifications of the documents to create an initial structure. There must be meta tags in the documents matching the classification (i.e. category: primers) for a classification names ‘category’. Default value = category,audience,folder

search.maxresults
: The number of search results to show per page of results

formatmask
: The format mask to use for timestamps. Note that this is a Java based format mask (i.e. MM is for month, mm for minutes. Check [SimpleDateFormatter](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) for more information about this mask). Default value = dd-MM-yyyy HH:mm:ss

versioncontrol.maxfilerevisions
: The maximum number of revisions to retrieve for the metadata page. Default = 10

versioncontrol.maxcontextrevisions
: The maximum number of revisions to collect / display for latest commits for the entire context (Revisions command). Default = 25

json.prettyprint
: When a page is rendered to JSON (by adding the mode=json parameter to a request) it will be pretty printed if this setting is set to ‘true’. Default = true

parsetimeout
: The maximum time in ms that a Markdown parse by the PegDown parser can last. Default = 4000

### File recognition
documents
: The extensions of the files that will be recognized as a 'Document' and therefore can be rendered to html, pdf etc. Default = marked,book,index,md

books
: The extensions of the files that will be recognized as a 'Book' and therefore shown by the auto generated index. Default = marked,book,index

index.extensions
: The extensions of file to include in the search index (comma separated; no ‘.’). Default = md,book,marked,txt

images.extensions
: Image recognition. Set (comma separated) the extensions (without the '.') below to determine whether a matching resource will be treated as an image. Default = png,jpeg,jpg,gif,tiff,bmp

### Markdown processing options
markdown.appenderrors
: Append any link / include error messages at the bottom of the document. Default = true

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
: Table support similar to what Multimarkdown offers. See [http://fletcherpenney.net/multimarkdown/users_guide/](http://fletcherpenney.net/multimarkdown/users_guide/). Default = on

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

## Sample configuration file
	#######
	# Primary options below; these you will have to set to get things going
	#######
	
	# The absolute path to the folder that will contain pulled contexts
	workspacelocation=/path/to/your/thoth/workspace
	
	
	#######
	# Repository and Context definitions. These specify where to get the content from
	# Can be as many repositories as you like. Make sure you number them sequentially and leave
	# no gaps (parsing will stop at the first entry that has it's name not set)
	#######
	
	# Name (identifier for use by Thoth) of the repository
	repository.1.name=Repository1
	
	# Type of version control. Currently only GIT and FS (simple filesystem) are supported
	repository.1.type=git
	
	# Location (URL or folder) of the repository that contains the Documentation
	repository.1.location=https://github.com/someuser/MyDocumentation.git
	
	# Username of the repository user
	repository.1.username=yourusername
	
	# Password of the repository user
	repository.1.password=yourpassword
	
	# The contexts to check out / pull from the repository.
	
	#The name of the context. Will be used as name for the local repository folder. Must be unique
	context.1.name=Context1
	
	#The name of the branch to check out
	context.1.repository=Repository1
	
	#The name of the branch to check out. Must be an existing branch in the associated repository
	context.1.branch=Branch1
	
	
	#######
	# Skinning related properties
	#######
	
	# The name of the default skin to use. If not set; it will use the builtin skin named 'Builtin' 
	# Note that any skin can come from the classpath as long as the package remains within net/riezebos/thoth/skins/
	skin.default=SimpleSkin
	
	# The skin to use for the main index page (hence not within a context). Must be a valid context name
	# as specified by the 'context.name' property. If left blank then the default skin is used.
	skin.mainindexcontext=
	
	# The URL for the localhost. Will be used for custom renderer processing
	localhost=http://localhost:8080/
	
	
	#######
	# Define any custom renderers below for for additional output formats. Make sure you number them sequentially and leave
	# no gaps (parsing will stop at the first custom renderer that has it's extension not set.
	# You can have any number of custom processors; just keep on numbering them.
	#
	# Note that you can also override the default html and raw renderers. Their extensions are 'raw' and 'html'
	#######
	
	renderer.1.extension=pdf 
	renderer.1.contenttype=application/pdf 
	renderer.1.command=/usr/local/bin/prince ${url}?suppresserrors=true -o ${output} --javascript --media=print --page-size=A4 --page-margin=20mm 
	
	renderer.2.extension= 
	renderer.2.contenttype= 
	renderer.2.command= 
	
	
	#######
	# Embedded server options. Not applicable when deployed as a WAR
	#######
	
	# The port to have the (embedded) server listen to. Default is 8080
	embedded.port=8080
	
	# The idle timeout for connections. Note: in specified in seconds
	embedded.idletimeout=30
	
	# Hostname of the server
	embedded.servername=localhost
	
	
	#######
	# Context index (page) options
	#######
	
	# The following comma separated classifications will be available to the context index template 
	# (grouping of documents by classification) The folder classification is built in; and is just listed below for clarity
	# In the template (or json) the classification names are available under "classification_" + <the name specified below>
	context.classifications=category,audience,folder
	
	
	#######
	# Search options
	#######
	
	# The number of search results per page; default is 25
	search.maxresults=25
	
	
	#######
	# More options below; but you might not necessarily have to change them
	#######
	
	# Default date format mask. Note that the month is MM and the minutes are mm. HH is 24hr and hh 12hr
	formatmask=dd-MM-yyyy HH:mm:ss
	
	# Pretty prent JSON responses. You might want to set this to false in a production environment; small performance benefit
	json.prettyprint=true
	
	# The maximum number of revisions to collect / display for latest commits per file (Meta command)
	versioncontrol.maxfilerevisions=10
	
	# The maximum number of revisions to collect / display for latest commits for the entire context (Revisions command)
	versioncontrol.maxcontextrevisions=25
	
	# Automatic refresh interval (seconds). Minimum is 30 seconds. Set to 0 for disable
	versioncontrol.autorefresh=60
	
	# The maximum time in ms that a Markdown parse can last
	parsetimeout=4000
	
	# The extensions of the files that will be recognized as a 'Document' and therefore can be rendered to html, pdf etc
	documents=marked,book,index,md
	
	# The extensions of the files that will be recognized as a 'Book' and therefore shown by the auto generated index
	books=marked,book,index
	
	# The extensions of file to include in the search index (comma separated; no '.')
	index.extensions=md,book,marked,txt
	
	# Append any link / include error messages at the bottom of the document
	markdown.appenderrors=true
	
	# Auto number headings up to the specified level. Default is 3, set to 0 to disable 
	markdown.maxheadernumberlevel=3
	
	#######
	# MARKDOWN OPTIONS BELOW
	# See https://github.com/sirthias/pegdown for a full description
	#######
	
	#Pretty ellipses, dashes and apostrophes.
	markdown.option.SMARTS=on
	
	#Pretty single and double quotes.
	markdown.option.QUOTES=on
	
	#PHP Markdown Extra style abbreviations.
	#See http://michelf.com/projects/php-markdown/extra/#abbr
	markdown.option.ABBREVIATIONS=on
	
	#Enables the parsing of hard wraps as HTML linebreaks. Similar to what github does.
	#See http://github.github.com/github-flavored-markdown
	markdown.option.HARDWRAPS=on
	
	#Enables plain autolinks the way github flavoured markdown implements them.
	#With this extension enabled pegdown will intelligently recognize URLs and email addresses
	#without any further delimiters and mark them as the respective link type.
	#See http://github.github.com/github-flavored-markdown
	markdown.option.AUTOLINKS=off
	
	#Table support similar to what Multimarkdown offers.
	#See http://fletcherpenney.net/multimarkdown/users_guide/
	markdown.option.TABLES=on
	
	#PHP Markdown Extra style definition lists.
	#Additionally supports the small extension proposed in the article referenced below.
	#See http://michelf.com/projects/php-markdown/extra/#def-list
	#See http://www.justatheory.com/computers/markup/modest-markdown-proposal.html
	markdown.option.DEFINITIONS=on
	
	#PHP Markdown Extra style fenced code blocks.
	#See http://michelf.com/projects/php-markdown/extra/#fenced-code-blocks
	markdown.option.FENCED_CODE_BLOCKS=on
	
	#Support [[Wiki-style links]]. URL rendering is performed by the active {@link LinkRenderer}.
	#See http://github.github.com/github-flavored-markdown
	markdown.option.WIKILINKS=on
	
	#Support ~~strikethroughs~~ as supported in Pandoc and Github.
	markdown.option.STRIKETHROUGH=on
	
	#Enables anchor links in headers.
	markdown.option.ANCHORLINKS=off
	
	#Suppresses HTML blocks. They will be accepted in the input but not be contained in the output.
	markdown.option.SUPPRESS_HTML_BLOCKS=off
	
	#Suppresses inline HTML tags. They will be accepted in the input but not be contained in the output.
	markdown.option.SUPPRESS_INLINE_HTML=off
	
	#Requires a space char after Atx # header prefixes, so that #dasdsdaf is not a header.
	markdown.option.ATXHEADERSPACE=off
	
	#Force List and Definition Paragraph wrapping if it includes more than just a single paragraph
	markdown.option.FORCELISTITEMPARA=off
	
	#Force List and Definition Paragraph wrapping if it includes more than just a single paragraph
	markdown.option.RELAXEDHRULES=off
	
	#GitHub style task list items: - [ ] and - [x]
	markdown.option.TASKLISTITEMS=on
	
	#Generate anchor links for headers using complete contents of the header
	markdown.option.EXTANCHORLINKS=off
	










