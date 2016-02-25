# Installing Thoth
There are two ways you can run Thoth. For server environments there is a WAR that you can deploy in a Web Container like tomcat and for desktop environments there is a standalone version that requires almost no setup.

## Configuration
Create a configuration.properties file as described in the [Configuration](Configuration.md) section.

## Running standalone
For desktop environments you can run Thoth without a Web Container using the Thoth-standalone version which has an embedded web server. First make sure you have Java8 installed, so [grab a JRE or JDK version 8](https://www.java.com/en/download/) and install it. Then you can start ‘standalone Thoth’ if you either have an environment or JVM variable set (as describe above) or simply provide the location of the configuration.properties file as an argument. Not even any of this is required if you have the configuration.properties file in the working folder when starting the Thoth standalone

	java -jar thoth-standalone-1.0.0.jar

By default the Thoth-standalone server is running in interactive mode (listening for commands on stdin). If you want to suppress the interactive mode and run Thoth-standalone purely as a (background process) server then add the `-server` flag:

	java -jar thoth-standalone-1.0.0.jar -server

## WAR installation
Have your Web Container and JDK8 ready and then just drop the Thoth WAR inside the web apps folder of your Web Container. And then the only requirement is that the configuration can be found through an environment variable (or -D provided JVM argument) called `thoth_configuration`. On servers with an init script for Tomcat (/etc/init.d/tomcat) you could add a script line stating 

	export thoth_configuration=/opt/conf/configuration.properties
