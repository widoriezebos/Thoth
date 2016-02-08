category: Intro
# Thoth Markdown basics
Currently Thoth uses the Markdown syntax as [defined originally by John Gruber](http://daringfireball.net/projects/markdown/syntax). On top of that Thoth supports some extra’s as defined by [PegDown](https://github.com/sirthias/pegdown) (depending how you have configured Thoth), [Critic Markup](http://criticmarkup.com) and the [Metadata definition](https://github.com/fletcher/MultiMarkdown/wiki/MultiMarkdown-Syntax-Guide) as defined by MultiMarkdown. The include processing is kept close to how you would specify in Latex.
Note that a lot of Markdown constructs require an empty line before it. If you see unexpected results with a header, a list or a table then usually this is resolved by placing an empty line directly about your construct.
Whitespace is generally not rendered. If you want an empty line between your paragraphs however you can simply enter two consecutive new-line characters.

## Table of Contents
You can add a generated TOC section to your document by adding \tableofcontents tag to your document. All headers present in the document will be used to create the TOC, using the level of the header to indent appropriately. Note that the \tableofcontents construct must appear (without any prefixes) at the beginning of a line.

	\tableofcontents

## Headers
Note: headers will be automatically numbered by Thoth (unless turned off in the configuration by setting markdown.maxheadernumberlevel = 0, default is 3). The following two methods of header specification are supported:

**SetExt style (either use a ‘-‘ or a ‘=‘)**

	My header
	=========

**Atx style (1 - 6 hashes to denote the level)**

	#My Level 1 header
	##My level 2 header
	...
	######My level 6 header

## Block quotes
You can use a \> character to start a block quote.

	> This is a block quote

## Lists
Lists can be unordered or ordered, and can be nested. Use asterisks, pluses, and hyphens — interchangably — as list markers, indent with a tab. Numbered lists should have a number suffixed with a ‘.’

	- Unordered 1
	- Unordered 2
	
	1. Ordered 1
	2. Ordered 2

## Code blocks
Pre-formatted code blocks can be used to include source code and will not be interpreted as Markdown. To create a code block simply indent the text with a Tab or with 4 spaces.

	   Code block
	   Taken literally 

## Horizontal Rules
You can include a horizontal rule in your output by placing three or more hyphens, underscores or asterisks in your text. 

	---

## Links
Note that Thoth currently only supports inline links because of complications that arise when files are included (currently this is a restriction). When referencing documents in the library, you can either use an absolute path or a relative path. The absolute path will take the location of your context as the root during resolving.
Note that Thoth supports ‘Soft links’ which means that you can define an alias or substitution pattern as a short-cut in a file called softlinks.properties in the root of your context. You reference a soft link by prefixing it’s name (or substitution pattern) with a ‘:’. You might prefer soft links over hard links if you want to avoid having hard links all over the place or want to use them simply as a shorthand. Reorganizing your library without breaking links all over the place can be avoided using soft-links.

	[Link text showing in the document](http://example.net/ "With a description")
	[A local document](nestedfolder/otherdocument.md)
	[A local document](:mysoftlink)

## Embedded images
If you prefix a link with a ‘!’ then it will embed the contents of that link as an image. The link can point to a local or remote resource. Description and title are optional when specifying an image.

	![Description of the image](images/Setup.png "Title of the image")
	![](images/Setup.png)

## Emphasis
You can emphasize pieces of your text by wrapping them inside either two ‘\*’ or ‘\_\’ characters

	This is an example of a *bold piece of text*
	Although you could also _do that this way_

## Code
You can mark text as code inside a single like enclosing it with a ‘\`’

	Use the `printf()` function.

## Backslash escapes
You can escape any character below that might be interpreted by Markdown otherwise:

	\   backslash
	`   backtick
	*   asterisk
	_   underscore
	{}  curly braces
	[]  square brackets
	()  parentheses
	#   hash mark
	+   plus sign
	-   minus sign (hyphen)
	.   dot
	!   exclamation mark

## Include processing
You can include any other markdown file (or any text file for that matter) by using a special include directive. The file specification can be either relative or absolute. Absolute paths will be taken as absolute from the context folder. 

	\include{somesubfolder/somedocument.md}
	\include{/absolute/path/to/somedocument.md}

## Critic markup
You can mark text with the syntax outlined below. Since this kind of markup is meant to work anywhere note that there is a space between the ‘{‘ and the directive to keep it from processing in this example. In your own markdown there should be *no space* after the opening ‘{‘ character. The Critic markup can be displayed during rendering by adding the request parameter `critics=show`, or displayed as-is with `critics=raw`. By default the critics are processed and the result will then be rendered.

	Addition { ++My inserted text++}
	Deletion { --My deleted text--}
	Substitution { ~~Original text~>Changed into something else~~}
	Comment { >>This is just a comment<<}
	Highlight { ==And this a highlighted piece of text, probably followed by a comment==}

## Meta data
You can add any number of meta data tags to your document. You do this by adding them at the very top of your document, separating the key from the value with a ‘:’ character. The first line that does not follow this rule will start normal Markdown processing. When including files, any meta tag in the included files is added if it is not encountered before. This means that meta tag with a specific key that is first encountered ‘wins’ and will not be overwritten by subsequent definitions. Meta data is searchable with the search engine and the key/values are also available to any template that wants to use them during rendering.

	audience: writers, developers
	title: Title of the document
	author: Wido Riezebos

## Tables
You can render a table using a ‘|’ character as a separator between columns (make sure the number of ‘|’ characters per row is always the same). You are required to define as header separator as the second line of your table definition, with at least one ‘-‘ as the contents. If you want alignment for a column use a ‘:’ in the separator row to specify where to align (:- for left, -: for right and :-: for center)

	|Code|Description|
	|-:|-|
	|1   | One       |
	|2   | Two       |

## Inline HTML
Although not encouraged, you can place HTML fragments directly in your markdown. Since this goes directly against the spirit of Markdown altogether this obviously should be used as the exception of the rule (i.e. title pages etc).

	<b>Any html</b>
