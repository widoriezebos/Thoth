# Features
## Markdown based editing
When writing a document you should not be distracted by form and layout, you should be able to focus on content. This is what Markdown enables you to do. In short Markdown is a wiki style of text editing that has very few rules for adding structure to a document. The content is stored as plain text, which makes it easy to process using a version control system for instance. Another benefit of using Markdown that it is kind of an emerging standard for markup. A lot of internet sites are using it, including Github, as their primary vehicle to editing documentation. It makes sense to use Markdown as the basis for a documentation system because there are a lot of editing solutions out there that support Markdown.

## Version control and collaboration
Since the content is stored as ‘Markdown fragments’, a version control system is very capable of making sense of changes. Thoth uses Git as the basis for it’s version control system. With that comes branching, merging, revision control and distributed collaboration out of the box. And when used correctly, maintaining a set of documentation for different versions of a product becomes relatively easy. What you would do is create a branch for every version of your product that is out there. Changes and additions to an older version of a document are then easy to track and merge to any other branch (for one of the newer versions). If you want or need this of course. You could also just use the version control system just as a way of making sure that nobody will ever inadvertently overwrite changes of a colleague.

## Document reuse and aggregation
When writing a large set of documents there will be several pieces of text that can be shared among them. Typically these are concepts and definitions that have to be part of various documents for different audiences. Screenshots are another example and when isolated they can easily be updated for a new version of product without affecting anything else. Thoth has a robust Include System that enables recursive include processing for Markdown documents. To ease maintenance of interdependencies (the links between document fragments or images) Thoth supports ‘Soft Links’ that are like an alias for a link. When you move a document fragment or rename it you will then only have to update the soft link. Any document that uses the Soft Link does not have to be touched.

## Content validation
Thoth validates links and bookmarks and is able to report on problems. This can be per document or for the entire library that you maintain.

## Skinning and templating
You can apply any number of skins to Thoth itself and the documents it can render. A skin can be automatically selected based on the location of a document in your library, so you can tailor the look and feel of a set of documents to your needs. Templates are written in the Velocity template syntax and skins support inheritance. When using multiple skins for your library you do not need to copy a lot of stuff around if you use skin inheritance; just add what you want to change to your new skin and specify from what skin it should inherit.

## Search engine
Thoth has an embedded search engine that automatically updates it’s index when changes are submitted (pushed) you the central documentation repository. The search engine supports complex expressions to help you narrow down when a simple query does not cut it.

## Meta data
You can add meta data to your documents that can help you organize your library. Documents can be categorized based on folder structure or meta tags and meta tags can be used in templates used for rendering your output

## Configurable and extendable rendering pipeline
Out of the box Thoth can render your Markdown documents to CSS styled HTML pages. By configuring additional outputs like PDF or EPUB you can easily render the same content to any format you want as long as you have a rendering tool that does that for you. Very good options here are [Prince](http://www.princexml.com), [Wk\<html\>to*pdf*](http://wkhtmltopdf.org) and [Pandoc](http://pandoc.org). As long as you have a command line tool that can render Markdown, CSS rich or plain HTML then you can plug it into Thoth.

## Open source, Apache license
Thoth is open source, free and available under the [Apache license](http://www.apache.org/licenses/LICENSE-2.0). This basically means you can do whatever you like with Thoth, including use in commercial environments as long as you adhere to the terms of the license.



