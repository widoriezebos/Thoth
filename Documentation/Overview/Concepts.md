# Concepts
In the documentation of Thoth (or some of the screens) there is reference of a few concepts that are defined below.

## The repository
A Repository is the entire set of documents that are related in some way. For unrelated documents it makes sense to set up more than one repository (for instance different sets of documentation for different products would have one repository per repository)
A Repository is published with Thoth using a corresponding Context (which basically is just a name for a repository). There are different kinds of repositories that Thoth supports: just a directory on your harddrive, a Git repository or a Zip somewhere on your filesystem.

## The Context
A Context exposes the content of a Repository through the Thoth (user) interface. You can also regard a Context as just a friendly name for your (potentially more technically named) Repository. Note that a Context is by definition unique within Thoth and is associated with exactly one Repository.

## The Fragment
A fragment is just a piece of text. It can be an entire chapter or just a short paragraph. It is up to you to determine the size and contents of your Fragments, basically Fragments are the building blocks of Books (or other fragments). The extension of a Fragment is ‘.md’ for Markdown (unless you changed that in the configuration).

## The Book
A Book is a special kind of Fragment because it is considered to be the root or container of included Fragments. This way Thoth can create indexes of Books automatically and determine whether there are any unused (dead) Fragments in your repository. The extension of a Book is ‘.book’ (unless you changed that in the configuration).

## Meta tags
Any fragment (and book) can contain Meta tags in it’s header. For one, any meta tag can be used for searching the repository (just enter a query in the form of ‘metatag:value’ to search for documents/fragments with that meta tag set). Other use of Meta tags is for instance when rendering the Context Index page (for instance showing the author of the document next to the title).

## Meta data
Thoth can also display additional Meta data about any document, showing document structure, the direct and indirect use of a fragment, revision history and any validation errors that occurred during include processing.

## Skin
Any screen in Thoth and any document / fragment that is rendered can be completely customized in terms of layout and visual design. The mechanism for this is called a Skin. Technically a Skin consists of a descriptor file, a few Velocity templates and potentially some additional resources (css, images, javascript). A Context can make use of any number of Skins, that can be selected based on the path of a certain Document or Fragment.



