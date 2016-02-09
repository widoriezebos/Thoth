
<p style="text-align:center;font-size:xx-large">
Thoth Documentation
</p>

<h1>Table of contents</h1>
<tableofcontents>
###[1  Copyright](#1copyright)


###[2  Introduction to Thoth](#2introductiontothoth)


###[3  What kind of a name is that?!](#3whatkindofanameisthat)


###[4  Features](#4features)

  - [4.1  Markdown based editing](#41markdownbasedediting)
  - [4.2  Version control and collaboration](#42versioncontrolandcollaboration)
  - [4.3  Document reuse and aggregation](#43documentreuseandaggregation)
  - [4.4  Content validation](#44contentvalidation)
  - [4.5  Skinning and templating](#45skinningandtemplating)
  - [4.6  Search engine](#46searchengine)
  - [4.7  Meta data](#47metadata)
  - [4.8  Supports Critic Markup](#48supportscriticmarkup)
  - [4.9  Configurable and extendable rendering pipeline](#49configurableandextendablerenderingpipeline)
  - [4.10  Open source, Apache license](#410opensourceapachelicense)

###[5  The Thoth Toolset](#5thethothtoolset)

  - [5.1  Git concepts](#51gitconcepts)
    - [5.1.1  Setting up your local Git repository](#511settingupyourlocalgitrepository)
    - [5.1.2  Starting SourceTree for the first time](#512startingsourcetreeforthefirsttime)
    - [5.1.3  Cloning the central repository](#513cloningthecentralrepository)
    - [5.1.4  SourceTree main screen](#514sourcetreemainscreen)
    - [5.1.5  Ready to start writing!](#515readytostartwriting)
  - [5.2  WYSIWYG](#52wysiwyg)
    - [5.2.1  Use the Thoth IncludeProcessor as a pre processor](#521usethethothincludeprocessorasapreprocessor)
    - [5.2.2  Use Thoth standalone with your browser](#522usethothstandalonewithyourbrowser)
    - [5.2.3  Use an editor that does WYSIWYG](#523useaneditorthatdoeswysiwyg)

###[6  Thoth Markdown basics](#6thothmarkdownbasics)

  - [6.1  Table of Contents](#61tableofcontents)
  - [6.2  Headers](#62headers)
  - [6.3  Block quotes](#63blockquotes)
  - [6.4  Lists](#64lists)
  - [6.5  Code blocks](#65codeblocks)
  - [6.6  Horizontal Rules](#66horizontalrules)
  - [6.7  Links](#67links)
  - [6.8  Embedded images](#68embeddedimages)
  - [6.9  Emphasis](#69emphasis)
  - [6.10  Code](#610code)
  - [6.11  Backslash escapes](#611backslashescapes)
  - [6.12  Include processing](#612includeprocessing)
    - [6.12.1  Include Markdown](#6121includemarkdown)
    - [6.12.2  Include Images](#6122includeimages)
    - [6.12.3  Include Source (code block)](#6123includesourcecodeblock)
  - [6.13  Critic markup](#613criticmarkup)
  - [6.14  Meta data](#614metadata)
  - [6.15  Tables](#615tables)
  - [6.16  Inline HTML](#616inlinehtml)

###[7  The configuration file](#7theconfigurationfile)

  - [7.1  Required settings](#71requiredsettings)
    - [7.1.1  Repository settings](#711repositorysettings)
    - [7.1.2  Context settings](#712contextsettings)
  - [7.2  Optional settings](#72optionalsettings)
    - [7.2.1  Custom renderers](#721customrenderers)
    - [7.2.2  File recognition](#722filerecognition)
    - [7.2.3  Markdown processing options](#723markdownprocessingoptions)
  - [7.3  Sample configuration file](#73sampleconfigurationfile)

###[8  Installing Thoth](#8installingthoth)

  - [8.1  Configuration](#81configuration)
  - [8.2  Running standalone](#82runningstandalone)
  - [8.3  WAR installation](#83warinstallation)
</tableofcontents>


[//]: # "Include /Copyright.md"

#1  Copyright

Copyright (c) 2016 W.T.J. Riezebos

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[//]: # "Include /Overview/Introduction.md"

#2  Introduction to Thoth

Before we delve into the details of what Thoth is all about, lets listen to a conversation between Dave (a Documentation Writer) and an Ian (an IT guy). So Dave comes in on a blue Monday morning after a well deserved and kind of relaxing weekend (I’ll spare you the details). In the coffee corner he finds Ian enjoying an espresso and while pressing the button for his first regular coffee of the day, Dave starts mumbling something about bloody Microsoft Word and revisions. We’re human beings. We all enjoy talking about the moments when tools let us down, especially on Monday mornings. So Ian joins in.

Still using Word for your work then? Well yes, although I am beginning to wonder why. I got a new version of the documentation from Sally and unfortunately she used an older version of the one I had. Merging her changes really made a mess; let alone the fact that for the new version of Product we use a new template now. To make things worse, parts of what she did should also be copied to other documents that mention the concepts. Ian takes a few seconds to respond, pondering whether to put on his consulting hat or start talking about one of his disastrous recovery attempts. No, too painful so the hat it is.

With two releases of product every year I guess that version control in Word simply does not cut it, Ian begins. Dave is about to explain the elaborate folder structure on the shared network drive but then tends to agree there. Nobody ever finds what they need anyways, especially when looking for screenshots of a particular version of the software.
What you need is a good version control system; something we in IT have had for decades Ian continues. With that in place you will never have a problem maintaining different versions of a particular manual and working an a document with Sally won’t be much of a problem either. And placing all of your content in a single document is not a good idea, you should be able to compose documents of smaller parts so that these can be shared across different documents tailored to different audiences.

In reality Dave does not know what to make of this. Seems like an intelligent thing to do but this also sounds like cocky nerd talk to him. And Dave is not a nerd. Dave is frustrated. And frustration is what drives him to continue the conversation.

Ok, so if I get this fancy merging version control system with these nice composition tricks in place what then? Apart from finally getting to grips with content and collaboration, are there more goodies IT can offer? Ian did not expect Dave to catch on this quickly and now get’s the notion of unexpected work coming his way. What Ian meant to achieve was more along the lines of showing off the tricks of his trade, not advertising for more work.

The way forward for Ian in these situations is a proven recipe: just add more to the mix until it all sounds like a very daunting task altogether. And then forget about it. Done.

So on Ian went: Searching and then finding what you need ‘Search Engine style’ of course. And being able to see who did what-when-to-which document etc. etc. And some smart templating rendering mechanism to publish the documents to HTML, PDF, EPUB and whatever you need. The possibilities are endless!

“Wow, sounds like you have experience there” Dave responds. Well, we use VI ourselves says Ian and he continues: that also does the trick. Kind of. By now Ian is hoping that this all sounds rather expensive and indeed to Dave it does. Mission accomplished.

So Dave presses the coffee button again and goes back to his personal Word hell for the week. Such is life.

But not for you, because you are reading this. A system described by Ian is not expensive at all: actually, it is here for free. It is Thoth.










[//]: # "Include /Overview/Name.md"

#3  What kind of a name is that?!

Yes, Thoth is not the easiest word to pronounce. (Unless copious amounts of beer are consumed because then almost anything you say automatically sounds like ‘Thoth’). When sobered up however it turns out that typing the name Thoth with your keyboard is remarkably easy. Just try it and I think you will agree. Easily writing down something that is not immediately clear when spoken is at the center of what makes a good documentation system. And it turns out that Thoth is also an Egyptian deity that (among a lot of other things) served as a Scribe to the Gods (Ok you got me there).




[//]: # "Include /Overview/Features.md"

#4  Features


##4.1  Markdown based editing

When writing a document you should not be distracted by form and layout, you should be able to focus on content. This is what Markdown enables you to do. In short Markdown is a wiki style of text editing that has very few rules for adding structure to a document. The content is stored as plain text, which makes it easy to process using a version control system for instance. Another benefit of using Markdown that it is kind of an emerging standard for markup. A lot of internet sites are using it, including Github, as their primary vehicle to editing documentation. It makes sense to use Markdown as the basis for a documentation system because there are a lot of editing solutions out there that support Markdown.


##4.2  Version control and collaboration

Since the content is stored as ‘Markdown fragments’, a version control system is very capable of making sense of changes. Thoth uses Git as the basis for it’s version control system. With that comes branching, merging, revision control and distributed collaboration out of the box. And when used correctly, maintaining a set of documentation for different versions of a product becomes relatively easy. What you would do is create a branch for every version of your product that is out there. Changes and additions to an older version of a document are then easy to track and merge to any other branch (for one of the newer versions). If you want or need this of course. You could also just use the version control system just as a way of making sure that nobody will ever inadvertently overwrite changes of a colleague.


##4.3  Document reuse and aggregation

When writing a large set of documents there will be several pieces of text that can be shared among them. Typically these are concepts and definitions that have to be part of various documents for different audiences. Screenshots are another example and when isolated they can easily be updated for a new version of product without affecting anything else. Thoth has a robust Include System that enables recursive include processing for Markdown documents. To ease maintenance of interdependencies (the links between document fragments or images) Thoth supports ‘Soft Links’ that are like an alias for a link. When you move a document fragment or rename it you will then only have to update the soft link. Any document that uses the Soft Link does not have to be touched.


##4.4  Content validation

Thoth validates links and bookmarks and is able to report on problems. This can be per document or for the entire library that you maintain.


##4.5  Skinning and templating

You can apply any number of skins to Thoth itself and the documents it can render. A skin can be automatically selected based on the location of a document in your library, so you can tailor the look and feel of a set of documents to your needs. Templates are written in the Velocity template syntax and skins support inheritance. When using multiple skins for your library you do not need to copy a lot of stuff around if you use skin inheritance; just add what you want to change to your new skin and specify from what skin it should inherit.


##4.6  Search engine

Thoth has an embedded search engine that automatically updates it’s index when changes are submitted (pushed) you the central documentation repository. The search engine supports complex expressions to help you narrow down when a simple query does not cut it.


##4.7  Meta data

You can add meta data to your documents that can help you organize (and search) your library. Documents can be categorized based on folder structure or meta tags and meta tags can be used in templates used for rendering your output. The syntax adheres to [MultiMarkdown](https://github.com/fletcher/MultiMarkdown/wiki/MultiMarkdown-Syntax-Guide) although currently metadata cannot span multiple lines.


##4.8  Supports Critic Markup

You can propose additions, deletions, changes and add comments like you would do in MS Word right in your markdown. You can then choose to have these to be part of the rendered output, or show them for review until either accepted in the final document version. The syntax is based on [Critic Markup](http://criticmarkup.com)


##4.9  Configurable and extendable rendering pipeline

Out of the box Thoth can render your Markdown documents to CSS styled HTML pages. By configuring additional outputs like PDF or EPUB you can easily render the same content to any format you want as long as you have a rendering tool that does that for you. Very good options here are [Prince](http://www.princexml.com), [Wk\<html\>to*pdf*](http://wkhtmltopdf.org) and [Pandoc](http://pandoc.org). As long as you have a command line tool that can render Markdown, CSS rich or plain HTML then you can plug it into Thoth.


##4.10  Open source, Apache license

Thoth is open source, free and available under the [Apache license](http://www.apache.org/licenses/LICENSE-2.0). This basically means you can do whatever you like with Thoth, including use in commercial environments as long as you adhere to the terms of the license.



[//]: # "Include /Basics/The Thoth Toolset.md"

#5  The Thoth Toolset

Let’s get familiar with the toolset. An important aspect of working with Thoth is version control, currently based on Git. Version control can be a bit daunting if you are new to the concept, but if you stick to the basics and use [a good Git client](https://www.sourcetreeapp.com) you will quickly get the hang of it. (Note: For very simple setups you can skip the use of Git altogether and use the File System based content manager)


[//]: # "Include /Basics/Git concepts.md"

##5.1  Git concepts

It is important to note that Git uses a local version control repository to store all it’s information. The local repository is usually created by ‘Cloning’ the central repository. After cloning the trick is that this local repository can be synchronized with the central repository. This synchronization is done by you using ‘Pull’ and ‘Push’. ‘Pull’ fetches information from the central repository and ‘Push’ writes your local changes to the central repository. Before you can ‘Push’ your changes however there are a few steps you need to do first:

1. You must commit your local changes to your local repository
2. You must then Pull any changes that happened since your last Pull from the central repository
3. If there are conflicting changes (i.e. both you and somebody else changed something on the same line in the same file) you have to resolve them. You do this by editing the file containing the conflict until you are satisfied, and then mark the file ‘resolved’ in Git. All your changes have to be committed before you continue.
4. You can now Push your local changes to the central repository. In rare cases (on a very busy central repository) somebody might have pushed changes while you were resolving conflicts; in that case you have to retry from step 2.


###5.1.1  Setting up your local Git repository

In the examples below we will use SourceTree as a Git client; but there is no pressing reason why you could not use any other Git client. The point is that you create a local Git repository where you will work on your documentation, and that you do this by cloning the central repository.


###5.1.2  Starting SourceTree for the first time

When you start SourceTree for the first time you are asked to add an account. If you are familiar with Github then this is where you your Github account details. If you are still wandering what Git actually is all about then you might need some help to either set up a central repository (Github would be good) or skip Git altogether and continue with a file based repository. If you go for File Based then you can read the text below just for reference, but you will be missing out on a lot of goodies.

![](../Basics/images/01%20Add%20Account.png)

Enter your account type and details and click ‘continue’.


###5.1.3  Cloning the central repository

The next step is about creating a local repository by cloning the central (remote) repository. Determine where you want to place your local repository (I usually create a folder Repositories in my home folder and put all repositories in there)

![](../Basics/images/02%20Clone.png)


###5.1.4  SourceTree main screen

After cloning the repository you should the main screen of SourceTree. A few things to note here:

![](../Basics/images/03%20Branches.png)

Under branches you see only one branch called ‘master’, and if you open up ‘remote’ you see more branches that are not yet available locally. You can have one branch at a time in your local repository, it is like a snapshot of a particular version of your files. The master branch is not the one you want to work on. Usually you work on develop or a branch based on develop. To switch to develop, just double click on the remove develop branch.

![](../Basics/images/04%20Checkout%20Branch.png)

After doing that you have switched to the develop branch. Now you should think about the branches that you want to have.

Typically you would have one repository for a set of documents that logically belong to each other (part of the same product for instance). In this repository you would then have one branch per version of the product. This makes it very easy to promote changes to an older version of the documentation to a new (even after the new branch was created) or back port documentation of a new version to an older version. Git makes it really easy to do by merging one branch into another and selecting the changes you want to have merged. This might sound a bit easier than it is in some cases but you should be able to find enough material on the web to become a Git expert in no time.

In any case, you should create a new branch based on develop and give it the name of a particular version of your (product) documentation. When you start working on the next version of the product documentation, you will create a new branch based on this initial version branch that you are about to create.

![](../Basics/images/05%20Create%20New%20Branch.png)


###5.1.5  Ready to start writing!

You have now your new branch active in your local repository and you can start creating and editing files (all located inside the repository folder, you remember where you put it right?). After creating files they will show up in SourceTree in the ‘Working copy’ section. Anything in the working copy is a local change that has not yet been committed to the local repository, so it is just a change in a file and nothing else. To add them to the (local) version control you will have to commit the changes. To do this check all the files listed in the Working Copy section, add a comment for your commit (good practice) and click ‘Commit’.

![](../Basics/images/07%20Commit%20Changes.png)

After you have committed your changes they are stored in the local repository and their state is therefore set to unchanged (as compared to what is stored in the local repository). The changes have not left your local machine however, this will only happen when you Push. Your mantra here is ‘Commit-Pull-Push’. You should always Pull before you Push. Git needs you to do this to make sure that any changes a colleague has pushed to the repository does not conflict with anything you are about to push.

![](../Basics/images/08%20Changes%20to%20Push.png)

Sidestep: If you forget to Pull before you commit, you might get an ugly error message (if you read carefully you will find ‘Please, commit your changes or stash them before you can merge’. To recover from this just close the dialog, commit your changes and then Pull again.

![](../Basics/images/11%20Pull%20before%20Commit.png)

Now click Pull and then Push to push your changes to the central repository. (Note the read badge with the number ‘1’ which shows you the number of local commits that are ready to get pushed)

![](../Basics/images/09%20Push.png)

After ‘pull/pushing’ your Working Copy will show empty again, all your changes are now available to any colleagues sharing the central repository with you (by Pulling in your changes).

![](../Basics/images/10%20No%20changes.png)

Now there might be a complication when a colleague did indeed push a change since the latest Pull you did, and that this change conflicts with a change of you. A conflict means that there is a change on the same line in the same file, and Git cannot automatically merge both changes. This so called ‘Merge Conflict’ will have to be resolved by you then.

![](../Basics/images/13%20Merge%20Conflicts.png)

If you have conflicts you head over to the Working Copy and find out what files have a conflict. There are several ways to resolve a conflict; the easiest one is overwriting either your own or your colleagues’ changes. That might be easy but is usually not the way forward because you might overwrite other changes in the file as well. You will either resolve the conflicts with your ‘External Merge Tool’ (on OSX that would be FileMerge), handpick the segments in SourceTree or you will simply head to your editor and find the conflicts in the source. After making sure everything is in order save the file and switch back to SourceTree. Now right click on the conflicted file and select ‘Mark Resolved’.

![](../Basics/images/15%20Mark%20Resolved.png)

Once every conflict has been resolved you can finally commit all your changes. You will notice that SourceTree enters a comment automatically about the merge and the conflicts

![](../Basics/images/16%20Commit%20Resolved.png)

Now Pull again to make sure nobody Pushed in the mean time and then Push yourself.






[//]: # "Include /Basics/Editing with Thoth.md"

##5.2  WYSIWYG

Although writing in Markdown is all about *not* focussing on how your document is rendered it is sometimes good to have a preview. Depending on the editor you would have some WYSIWYG functionality in place, but it will most certainly not do all that Thoth can do for you. To get around this there are a couple of options.


###5.2.1  Use the Thoth IncludeProcessor as a pre processor

For OSX there is the excellent tool called Marked2 that supports a custom pre processor. By making use of this feature you can actually run any Thoth specific functionality (includes, link processing etc) right before it is transformed into HTML. To give you a sense of what a pre processor’s role here could be just have a look at the screenshot below. Any editor that supports a pre processor can be set up this way.

![](../Basics/images/MarkedPreprocessor.png)

The command line that is obscured in the screenshot should be similar to

	-cp /Users/wido/Libs/thoth-lib.jar net.riezebos.thoth.markdown.IncludeProcessor


###5.2.2  Use Thoth standalone with your browser

You can easily run Thoth as a standalone server on your laptop, using a File System based repository that points straight into your Git documentation repository. Read the section about configuring and installing Thoth on how to do this. Basically you just connect your browser to the locally running Thoth that will show you exactly what your rendered document looks like. Just save your document, switch to your browser and press the refresh-key. To do this; open a terminal and enter:

**`java -jar thoth-standalone.jar /path/to/my/config.properties`**

	Thoth is firing up. Please hang on...
	Setting up content managers...
	Thoth server started.
	You can now access Thoth at http://localhost:8080
	Just enter 'stop' to stop the server
	


###5.2.3  Use an editor that does WYSIWYG

If you use an editor that supports WYSIWYG and supports a pre processor as described above then you are set to go. If the editor does not support a pre processor however there will be certain features (include processing being the obvious one) that will not show up in the WYSIWYG (therefore breaking the WYG part). The WYSIWYG will then maybe helpful but it will not show you the whole story.


[//]: # "Include /Basics/Thoth Markdown basics.md"

#6  Thoth Markdown basics

Currently Thoth uses the Markdown syntax as [defined originally by John Gruber](http://daringfireball.net/projects/markdown/syntax). On top of that Thoth supports some extra’s as defined by [PegDown](https://github.com/sirthias/pegdown) (depending how you have configured Thoth), [Critic Markup](http://criticmarkup.com) and the [Metadata definition](https://github.com/fletcher/MultiMarkdown/wiki/MultiMarkdown-Syntax-Guide) as defined by MultiMarkdown. The include processing is kept close to how you would specify in Latex.
Note that a lot of Markdown constructs require an empty line before it. If you see unexpected results with a header, a list or a table then usually this is resolved by placing an empty line directly about your construct.
Whitespace is generally not rendered. If you want an empty line between your paragraphs however you can simply enter two consecutive new-line characters.


##6.1  Table of Contents

You can add a generated TOC section to your document by adding \tableofcontents tag to your document. All headers present in the document will be used to create the TOC, using the level of the header to indent appropriately. Note that the \tableofcontents construct must appear (without any prefixes) at the beginning of a line.

	\tableofcontents


##6.2  Headers

Note: headers will be automatically numbered by Thoth (unless turned off in the configuration by setting markdown.maxheadernumberlevel = 0, default is 3). The following two methods of header specification are supported:

**SetExt style (either use a ‘-‘ or a ‘=‘)**

	My header
	=========

**Atx style (1 - 6 hashes to denote the level)**

	#My Level 1 header
	##My level 2 header
	...
	######My level 6 header


##6.3  Block quotes

You can use a \> character to start a block quote.

	> This is a block quote


##6.4  Lists

Lists can be unordered or ordered, and can be nested. Use asterisks, pluses, and hyphens — interchangably — as list markers, indent with a tab. Numbered lists should have a number suffixed with a ‘.’

	- Unordered 1
	- Unordered 2
	
	1. Ordered 1
	2. Ordered 2


##6.5  Code blocks

Pre-formatted code blocks can be used to include source code and will not be interpreted as Markdown. To create a code block simply indent the text with a Tab or with 4 spaces.

	   Code block
	   Taken literally


##6.6  Horizontal Rules

You can include a horizontal rule in your output by placing three or more hyphens, underscores or asterisks in your text.

	---


##6.7  Links

Note that Thoth currently only supports inline links because of complications that arise when files are included (currently this is a restriction). When referencing documents in the library, you can either use an absolute path or a relative path. The absolute path will take the location of your context as the root during resolving.
Note that Thoth supports ‘Soft links’ which means that you can define an alias or substitution pattern as a short-cut in a file called softlinks.properties in the root of your context. You reference a soft link by prefixing it’s name (or substitution pattern) with a ‘:’. You might prefer soft links over hard links if you want to avoid having hard links all over the place or want to use them simply as a shorthand. Reorganizing your library without breaking links all over the place can be avoided using soft-links.

	[Link text showing in the document](http://example.net/ "With a description")
	[A local document](nestedfolder/otherdocument.md)
	[A local document](:mysoftlink)


##6.8  Embedded images

If you prefix a link with a ‘!’ then it will embed the contents of that link as an image. The link can point to a local or remote resource. Description and title are optional when specifying an image.

	![Description of the image](images/Setup.png "Title of the image")
	![](images/Setup.png)


##6.9  Emphasis

You can emphasize pieces of your text by wrapping them inside either two ‘\*’ or ‘\_\’ characters

	This is an example of a *bold piece of text*
	Although you could also _do that this way_


##6.10  Code

You can mark text as code inside a single like enclosing it with a ‘\`’

	Use the `printf()` function.


##6.11  Backslash escapes

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


##6.12  Include processing

There are three different ways of including content in your document; tailored to the type of included content: MarkDown, images or raw source code.


###6.12.1  Include Markdown

You can include any other markdown file (or any text file for that matter) by using a special include directive. The file specification can be either relative or absolute. Absolute paths will be taken as absolute from the context folder. You can provide an optional second argument that specifies the level adjustment for headers (negative or positive). When given a value of 1 the headers of the included file will be bumped up 1 level when including them in the result.

	\include{somesubfolder/somedocument.md}
	\include{somesubfolder/somedocument.md, 1}
	\include{/absolute/path/to/somedocument.md}


###6.12.2  Include Images

There is also include functionality for images using a wildcard. This enables you to add images from a folder without having to completely specifying their name. The header level for automatically determined headers can be specified as a separate argument. Specify 0 for no headers.

	\includeimages{images/component/*.png, 1}


###6.12.3  Include Source (code block)

You can include any text file as a Code Block, Thoth will prefix every line with a tab character making it a Code Block

	\includecode{sources/SomeFile.java}


##6.13  Critic markup

You can mark text with the syntax outlined below. Since this kind of markup is meant to work anywhere note that there is a space between the ‘{‘ and the directive to keep it from processing in this example. In your own markdown there should be *no space* after the opening ‘{‘ character. The Critic markup can be displayed during rendering by adding the request parameter `critics=show`, or displayed as-is with `critics=raw`. By default the critics are processed and the result will then be rendered.

	Addition { ++My inserted text++}
	Deletion { --My deleted text--}
	Substitution { ~~Original text~>Changed into something else~~}
	Comment { >>This is just a comment<<}
	Highlight { ==And this a highlighted piece of text, probably followed by a comment==}


##6.14  Meta data

You can add any number of meta data tags to your document. You do this by adding them **at the very top** of your document, separating the key from the value with a ‘:’ character. The first line that does not follow this rule will start normal Markdown processing. When including files, any meta tag in the included files is added if it is not encountered before. This means that meta tag with a specific key that is first encountered ‘wins’ and will not be overwritten by subsequent definitions. Meta data is searchable with the search engine and the key/values are also available to any template that wants to use them during rendering.

	audience: writers, developers
	title: Title of the document
	author: Wido Riezebos


##6.15  Tables

You can render a table using a ‘|’ character as a separator between columns (make sure the number of ‘|’ characters per row is always the same). You are required to define as header separator as the second line of your table definition, with at least one ‘-‘ as the contents. If you want alignment for a column use a ‘:’ in the separator row to specify where to align (:- for left, -: for right and :-: for center)

	|Code|Description|
	|-:|-|
	|1   | One       |
	|2   | Two       |


##6.16  Inline HTML

Although not encouraged, you can place HTML fragments directly in your markdown. Since this goes directly against the spirit of Markdown altogether this obviously should be used as the exception of the rule (i.e. title pages etc).

	<b>Any html</b>

[//]: # "Include /Setup/Configuration.md"

#7  The configuration file

In this section you will find the settings Thoth supports. The configuration of Thoth is placed in a property file which is read during startup. Without the minimal configuration (which is about where to store working files and where to find content) Thoth will not be able to start


##7.1  Required settings

workspacelocation
: States where the working files of Thoth will be created. This is where Thoth will checkout branches and create search indexes. In principle it is completely safe to delete the entire contents of the workspace (after shutting down Thoth). When Thoth is launched it will automatically recreate the contents of the workspace.


###7.1.1  Repository settings

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


###7.1.2  Context settings

A context if the root of a library, and corresponds to a branch in Git. For FileSystem  repositories there is not branch, but the name of the context is used in Thoth to refer to the contents. Note the numeric index in the name (1) which enumerates the context definition. You can define as many contexts as long as you number them sequentially leaving no gaps. Thoth will stop at the first context.*n*.name that does not have it’s value set.

context.1.name
: The uniquely identifying name of a context. This name will be part of any path to a document inside the context; and the main index page of Thoth by default displays a list of known contexts.

context.1.repository
: The reference (by name) to the repository where the contents of this context resides.

context.1.branch
: The name of the branch to pull from Git. In the case of FS repository this can be left blank.

context.1.refreshseconds
: The number of seconds between automatic repository refreshes. When a change is detected an indexer updating the search index is automatically launched. Default = 60


##7.2  Optional settings

skin.default
: The name of the default skin. When no skin is defined for a specific context this is the skin that will be used. Also used when no skin is defined for the main index page. Default value is ‘SimpleSkin’

skin.mainindexcontext
: Use the skin associated with the given context for the main index page. If left blank the skin.default property will be used as a fallback. Default value is \<no value\>

localhost
: Custom rendering uses a forked process to render the contents. When this process needs to fetch the contents; this URL is used as the base for the path of the document. Default value is ‘http://localhost:8080/'


###7.2.1  Custom renderers

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

formatmask.timestamp
: The format mask to use for timestamps. Note that this is a Java based format mask (i.e. MM is for month, mm for minutes. Check [SimpleDateFormatter](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) for more information about this mask). Default value = dd-MM-yyyy HH:mm:ss

formatmask.date
: The format mask to use for dates (without the time part). Note that this is a Java based format mask (i.e. MM is for month, mm for minutes. Check [SimpleDateFormatter](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) for more information about this mask). Default value = dd-MM-yyyy

versioncontrol.maxfilerevisions
: The maximum number of revisions to retrieve for the metadata page. Default = 10

versioncontrol.maxcontextrevisions
: The maximum number of revisions to collect / display for latest commits for the entire context (Revisions command). Default = 25

json.prettyprint
: When a page is rendered to JSON (by adding the mode=json parameter to a request) it will be pretty printed if this setting is set to ‘true’. Default = true

parsetimeout
: The maximum time in ms that a Markdown parse by the PegDown parser can last. Default = 4000


###7.2.2  File recognition

documents
: The extensions of the files that will be recognized as a 'Document' and therefore can be rendered to html, pdf etc. Default = marked,book,index,md

books
: The extensions of the files that will be recognized as a 'Book' and therefore shown by the auto generated index. Default = marked,book,index

index.extensions
: The extensions of file to include in the search index (comma separated; no ‘.’). Default = md,book,marked,txt

images.extensions
: Image recognition. Set (comma separated) the extensions (without the '.') below to determine whether a matching resource will be treated as an image. Default = png,jpeg,jpg,gif,tiff,bmp


###7.2.3  Markdown processing options

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


##7.3  Sample configuration file

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
	
	# Default timestamp format mask. Note that the month is MM and the minutes are mm. HH is 24hr and hh 12hr
	formatmask.timestamp=dd-MM-yyyy HH:mm:ss
	
	# Default date format mask. Note that the month is MM and the minutes are mm.
	formatmask.date=dd-MM-yyyy
	
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









[//]: # "Include /Setup/Installation.md"

#8  Installing Thoth

There are two ways you can run Thoth. For server environments there is a WAR that you can deploy in a Web Container like tomcat and for desktop environments there is a standalone version that requires almost no setup.


##8.1  Configuration

Create a configuration.properties file as described in the [Configuration](../Setup/Configuration.md) section.


##8.2  Running standalone

For desktop environments you can run Thoth without a Web Container using the Thoth-standalone version which has an embedded web server. First make sure you have Java8 installed, so [grab a JRE or JDK version 8](https://www.java.com/en/download/) and install it. Then you can start ‘standalone Thoth’ if you either have an environment or JVM variable set (as describe above) or simply provide the location of the configuration.properties file as an argument. Not even any of this is required if you have the configuration.properties file in the working folder when starting the Thoth standalone

	java -jar thoth-standalone.jar


##8.3  WAR installation

Have your Web Container and JDK8 ready and then just drop the Thoth WAR inside the web apps folder of your Web Container. And then the only requirement is that the configuration can be found through an environment variable (or -D provided JVM argument) called `thoth_configuration`. On servers with an init script for Tomcat (/etc/init.d/tomcat) you could add a script line stating

	export thoth_configuration=/opt/conf/configuration.properties


