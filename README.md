# Thoth
Simple yet powerful markup based documentation system

COPYRIGHT
---- 
Copyright (c) 2016 W.T.J. Riezebos

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0][1]

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

ACKNOWLEDGMENTS
---- 

Thoth would not have been possible without the great help of the following libraries (in arbitrary order):

- Pegdown Markdown parser, [https://github.com/sirthias/pegdown][2]
- JGit, [http://www.eclipse.org/jgit/][3]
- Diff-match-patch, [https://code.google.com/archive/p/google-diff-match-patch/][4]
- Apache Lucene, [https://lucene.apache.org/core/][5]
- And of course many many other open source projects. Check the pom.xml for a complete list. 

GETTING STARTED
---- 

To be able to launch Thoth inside Tomcat (either standalone or in Eclipse) just follow these simple steps:

1. Copy the sample.configuration.properties file from the conf folder to anywhere on your machine.  
	For example place it here: /Users/wido/Documents/Settings/wido.configuration.properties
2. Open your property file and start setting the required properties:
3. Determine where to store the working information (local Git repositories managed by Thoth)  
	The property to set is workspacelocation
4. Set the git.username, git.password and git.repository properties to their correct values.  
	For instance:   
	`git.repository=https://my.gitserver.com/mycontext/documentation.git`  
	`git.username=myuser`  
	`git.password=mysecret`
5. Add a system variable or a system property to your launch configurationso that Thoth  
	can find your configuration file:  
	-Dconfiguration=/Users/wido/Documents/Settings/wido.configuration.properties
6. Launch Tomcat. Your branches will be checked out to local Git repositories in your  
	workspace and they will be indexed automatically. When this is done you can connect  
	with your browser and get going

[1]:	http://www.apache.org/licenses/LICENSE-2.0
[2]:	https://github.com/sirthias/pegdown
[3]:	http://www.eclipse.org/jgit/
[4]:	https://code.google.com/archive/p/google-diff-match-patch/
[5]:	https://lucene.apache.org/core/
