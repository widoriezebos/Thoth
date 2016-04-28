# Thoth
Simple yet powerful Markdown based Documentation System for professional documentation writers. (Although serious bloggers might like Thoth too)

## In a hurry?
- Markdown (text based) editing helps you focus on content and structure so you can ignore layout while writing
- Supports version control, revisions and branching
- Plays nice with formal review processes. Just use Crucible!
- Multiple writers can work on the same document simultaneously
- A document can include any other document as a fragment, so reuse across documents for different audiences is not causing a headache
- Supports the notion of a library; referencing documents or images using an absolute or relative path
- Supports Soft Links so that changing the location of a fragment does not break documents referencing it
- Powerful indexer and search engine (based on Lucene)
- Provides meta data for documents, tagging your documents and providing instant insight into what document is referencing or is referenced by any other document
- Supports templates and skinning; full control over how your Markdown is rendered to HTML or PDF (could be anything else actually, want EPUB?)
- Open source, Apache license 2.0 so do whatever you like with it

## Getting started, while still in a hurry
Thoth runs inside a web container like Tomcat or Jetty. To be able to launch Thoth inside Tomcat (either standalone or in Eclipse) just follow these simple steps:

1. Copy the sample.configuration.properties file from the conf folder to anywhere on your machine. In this example we copy/rename it here: `/Users/wido/Settings/wido.configuration.properties`
2. Open your property file and start setting the required properties:
3. Determine where to store the working information (local Git repositories managed by Thoth, one per branch). The property to set is workspacelocation. For instance:  
	`workspacelocation=/Users/wido/Documents/ThothWorkspace`
4. Set the properties below to their correct values. For instance:  
	`repository.1.name=MyRepository`  
	`repository.1.type=git`  
	`repository.1.location=https://github.com/widoriezebos/Thoth.git`  
	`repository.1.username=mygituser`  
	`repository.1.password=mysecret`  
	`context.1.name=HelloThoth`  
	`context.1.repository=MyRepository`  
	`context.1.branch=master`  
	`context.1.library=Documentation`
5. Decide how you want to run Thoth.  
	If you want to use the **simplest approach** (i.e. running just on your desktop) then just place the configuration.properties in your working folder and from there launch Thoth standalone (or pass the path to your property file as an argument) i.e.  
	`java -jar <path-to-thoth-standalone.jar>` and off you go.

	Thoth can also be installed into a **web container** like Tomcat by simply dropping in the WAR. If you want to use the WAR (say with Tomcat):
	- Place the Thoth WAR inside the `webapps` folder of Tomcat
	- Add a system variable or a system property to your launch configuration so that Thoth can find your configuration file. So launch the Java VM with an additional parameter:  
		`-Dthoth_configuration=/Users/wido/Settings/wido.configuration.properties`
	- Launch Tomcat.
6. Your branches pulled to to local Git repositories in your workspace and they will be indexed automatically. When this is done you can connect  
	with your browser and get going!

## Not in a hurry?
**There is actually proper documentation** so why don't you head on to [the full documentation of Thoth][1]?

## Copyright
Copyright (c) 2016 W.T.J. Riezebos

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0][2]

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Acknowledgements
Thoth would not have been possible without the great help of the following projects (in arbitrary order):

- Markdown, [https://daringfireball.net/projects/markdown/][3]
- Pegdown Markdown parser, [https://github.com/sirthias/pegdown][4]
- JGit, [http://www.eclipse.org/jgit/][5]
- Diff-match-patch, [https://code.google.com/archive/p/google-diff-match-patch/][6]
- Apache Lucene, [https://lucene.apache.org/core/][7]
- And of course many many other open source projects. Check the pom.xml for a complete list. 

[1]:	http://thoth.riezebos.net/Thoth
[2]:	http://www.apache.org/licenses/LICENSE-2.0
[3]:	https://daringfireball.net/projects/markdown/
[4]:	https://github.com/sirthias/pegdown
[5]:	http://www.eclipse.org/jgit/
[6]:	https://code.google.com/archive/p/google-diff-match-patch/
[7]:	https://lucene.apache.org/core/