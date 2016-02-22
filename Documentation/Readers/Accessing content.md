# Thoth content
## Everything is a document
Every piece of text that is stored as a file in a Thoth repository is considered to be a Document. Documents can include or reference other documents, and this implies a hierarchy that is made a bit more explicit in Thoth by defining the concepts of Fragments and Books. In short a Fragment is just a piece of text that can (and should) be included or referenced by another Fragment or a Book. A Book is in this sense the ‘parent’ of at least one (text) Fragment. By this definition any fragment that is not referenced is a ‘dead’ piece of text because it is not used anywhere. A Book is therefore the only kind of document that is intended to be read by some audience. Fragments are in turn the building blocks that make up Books.

## Accessing content through Thoth
Thoth provides easy access to the content that is stored in a repository. The way the content is accessible is highly customizable and depends on the Skin that is selected. On a high level however there are the following ways of finding information stored in Thoth

- Access Books through the main index of a Context (a repository).
- Search the entire context using the search engine
- Browse through the folders of the entire context
- Follow the structure of a book or document through it’s Meta Data

## Rendering different formats
Thoth is capable of rendering to almost any format you like as long as there is a command line tool that can be hooked up. For PDF you can setup Thoth to use PrinceXML or WkHtmlToPdf and Pandoc has a converter for almost anything else (Docx, EPub