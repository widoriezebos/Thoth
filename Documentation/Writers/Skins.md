# Choosing a Skin
Thoth comes equipped with built-in skins that enable you to change the appearance of screens and documents. Basically a Skin is a collection of templates, CSS and any other resource files required to render an HTML page. By default the Skin named SimpleSkin will be chosen to render all screens and documents in Thoth. In the configuration you can change the default Skin, but it is also possible to influence the selection of the Sin in other ways

## Overriding the Skin dynamically
You can force Thoth to use a skin named ‘MySkin’ by adding a request parameter `skin=MySkin` 
An example forcing the (builtin) Skin named Bootstrap would be

	http://localhost:8080/skin=Bootstrap

## Overriding the Skin based on the path of a document (automatically)
In the root of your library you (should) have a plain text document called skins.properties and in this file you associate a Skin with a path. An example of this is 

	*/Datamodel/*=DataModelSkin
	*=SimpleSkin

In this example any path that matches \*/Datamodel/\* will be rendered using the DataModelSkin. As a fallback any other path will be associated with SimpleSkin. Matching is done from top to bottom and the first match wins.

# Creating your own Skin
Of course you can use the builtin Skins (SimpleSkin and Bootstrap) but you can define as many as you like yourself. You do this by creating a Skin descriptor file and placing this file anywhere in your library. Thoth will find all your Skin descriptors and enable them for use within your library.

## Creating the Skin descriptor file
The Skin descriptor file is a property file with the name `skin.properties`. Inside this file you must at least have one property that defines the name of the Skin. If you leave out everything else you have effectively defined a new Skin that inherits everything from SimpleSKin. So the minimal skin.properties file looks like 

	# The name of the skin. Used for referencing this skin in the configuration
	name=MyOwnSkin

Of course it does not make any sense to do this because there is no difference from the SimpleSkin, so let’s have a look at a better example:

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

Note the inheritsfrom setting. If you leave it empty then you inherit from SimpleSkin. If you put a name of another (custom) Skin there, you can inherit from that Skin. This enables you to inherit from a base Skin without having to copy anything that you did not change.

The templates specified in the above example are Velocity templates that render the various pages and documents for Thoth. The paths of the templates are relative to the skin.properties file, it is good practice to place the files directly where the skin.properties file is located, and put all of them in a folder with the name of the Skin.

By changing these templates you can completely alter the way Thoth looks and feels. See the Skin Developers Manual for more information on how to create your own templates.
