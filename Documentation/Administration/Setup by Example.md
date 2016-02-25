# Setting up your first library
Your library is the folder that is the root for all your documents. In the root of your library you will want to place (at least) to files: 

- `softlinks.properties` which will contain the aliases for hard links inside or external to your library. Even if you do not have any soft links yet it is still good idea to create an (empty) version of this file.
- `skins.properties` which associates a Skin with a particular path in your library. By default (if you do not have a skins.properties) then all paths are mapped to SimpleSkin which is the same as having a skins.properties with one line in it:

`*=SimpleSkin`

## The structure of your library
The structure of your library (in terms of documentation files and folders) is important. Depending on your needs to might want to structure your library around functional areas and reuse. If you are working with a lot of writers, structuring around functionality makes it easier to find (or determine where to place) documents. Of course you can include any document fragment regardless where it is located in your library, but it will help you organize if you apply some general rule about a functional hierarchy.
In terms of reuse you could also have a folder called ‘Library’ inside the root library. It them makes sense to place anything that is a sure candidate for a lot of reuse. Examples are definitions, disclaimers, various images (logo’s) etc.

If you make use of softlinks then reorganizing your library will be a lot easier so it is a good idea to start using them right away.

## Soft Links
You can define an alias for a fully qualified link (either inside the library, or posting to somewhere on the internet). Thoth refers to these aliases as softlinks, because they make it easier to manage hard links: inside your library you will always use the softlink to refer to  the hard link; changing a hard link (because you restructured your library or something on an external site changed) there is one place where you need to change something: the definition of the softlink inside the `softlinks.properties` file. Good examples for use of softlinks are frequently used images and pointers into a folder structure based on a naming convention (generated class model documentation for instance).

	tip=/Library/Images/tip.png
	@*=/Library/Classmodel/$1/Class.md

where you would use :tip and :@Order for softlinks. Note the use of the wildcard (‘\*’) and the back reference $1 in the second example. The ‘\*’ matches everything after (in this case) an ‘@‘ sign and is then used to translate the result into a fully qualified link into the Classmodel section of the library. 

# Usage scenarios
## Editing directly in your Git repository with WYSIWYG in your Browser
In this usage scenario you manage your version control with your Git client and you edit your files directly in your local Git repository. You will use Thoth-Standalone to serve you the contents of your Git repository straight to your browser.

### Git repository
In this usage scenario the location of your Git repository is /Users/wido/Repositories/Thoth/Documentation and you will serve the content for Thoth-Standalone directly from there. To be able to do this we choose a Repository type = “Filesystem” in stead of “Git”. Net effect is that Thoth will not be synchronizing anything and will be able to display the contents of your files directly, a very simple setup.  Committing and pushing your changes to the central Git repository is done just as you would normally do that. Note that with a repository of type ‘Filesystem’ Thoth will not be able to show you any revision history.

### Thoth Standalone
To preview your changes you run Thoth-Standalone on your desktop, with a browser connected to it on the standard port 8080. If you like you can choose any other port, refer to the Configuration section on how to do that. Create a configuration file with the following contents (adjust the paths for your local setup) and save it as `configuration.properties`

\includecode{thoth-standalone.properties}

Then launch Thoth-Standalone. The easies way is to open a terminal / console window and change to the same folder where you stored your configuration file. Reason for this is simple: if the working directory of Thoth contains a file called `configuration.properties` then it will use that if no arguments were provided. SO

With the current directory changed to where you stored `configuration.properties` you simply type

	java -jar /pathto/thoth-standalone-1.0.0.jar

If you wan to be more specific about starting Thoth then you would do something like

	java -jar /pathto/thoth-standalone-1.0.0.jar /pathto/configuration.properties

Thoth will start and display something similar to

	Thoth standalone v1.0.0
	Server is firing up. Please hang on...
	Setting up content managers...
	Thoth server started.
	You can now access Thoth at http://localhost:8080
	Just enter 'stop' to stop the server

Then start your browser and visit http://localhost:8080 to find your content. Just refresh any page you have open to render any changes you have saved.

## Editing locally, WYSIWYG with Marked2 (OSX)
In this scenario you most likely will use a Markdown editor with does not do any WYSIWYG but you have Marked2 for that. You have Marked2 setup with Thoth as a pre-processor so that all of the Thoth features are available to Marked2. You will not have to trigger refreshes for files that are opened in Marked2 since Marked2 will detect that for you. If the file you are editing is however nested deep, Marked2 will not auto detect and you will have to refresh manually just like you would with a regular browser.

### Settings in Marked2
In the preferences of Marked2 you go to Advanced, and then select the Preprocessor tab. There you check the ‘Enable Custom Preprocessor’ and enter `/usr/bin/java` in the Path field and something similar to `-cp /Users/wido/Applications/thoth-lib-1.0.0.jar net.riezebos.thoth.markdown.IncludeProcessor` in the Args field.
Note that the Thoth IncludeProcessor detects the root of your library by trying to locate the `softlinks.properties` file. If you do not have such a file (why not?) then you need to specify the location of your library with an extra argument: `-library /path/to/libroot`

## Editing locally, render on a remote server
In this scenario you have a local and a central Git repository and a Thoth server somewhere on the internet. When you commit and push your changes they will be automatically synchronized with the Thoth server (that will periodically pull any changes). of course this scenario could be a mix of any of the above scenario’s, but this is just to highlight the use of a central Thoth server that publishes your committed/pushed content.