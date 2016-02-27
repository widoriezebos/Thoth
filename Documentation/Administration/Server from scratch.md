# Setting up a server from scratch
In this chapter we will set up a Linux server with all the software required to run Thoth in a web container (so we will not be using the standalone server but Tomcat).

## Amazon Linux AMI
For this example we will use an Amazon Linux image as the basis. We will start doing some groundwork like setting the correct timezone and installing Apache HTTPD

	sudo su - root
	ln -sf /usr/share/zoneinfo/Europe/Amsterdam /etc/localtime

### Setting up the directory for downloads
	cd /opt
	mkdir download

### Installing the Java8 JDK
	cd /opt/download
	wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u74-b02/jdk-8u74-linux-x64.rpm
	rpm -i jdk-8u74-linux-x64.rpm  

### Installing Apache HTTPD
	yum install httpd
	chkconfig httpd on
	chkconfig --level 2345 httpd on
	vi /etc/httpd/conf.d/mod_proxy.conf

and then enter

	<Location />
	    ProxyPass ajp://localhost:8009/
	</Location>

### Installing Apache Tomcat
We will use Tomcat v8 for this setup.

	cd /opt/download/
	wget http://mirror.nl.webzilla.com/apache/tomcat/tomcat-8/v8.0.32/bin/apache-tomcat-8.0.32.zip
	cd /opt
	unzip download/apache-tomcat-8.0.32.zip
	chmod 755 /opt/apache-tomcat-8.0.32/bin/*.sh

Create the service script:

	vi /etc/init.d/tomcat

and then enter

\includecode{tomcat.txt}

And then make this file executable and add it as a service:

	chmod 755 /etc/init.d/tomcat 
	chkconfig --add /etc/init.d/tomcat
	chkconfig --level 2345 tomcat on

Now let’s remove all default contexts that come with Tomcat since we won’t be needing them:

	rm -rf /opt/apache-tomcat-8.0.32/webapps/*

### Installing PrinceXML (PDF renderer)
If you want to render your Markdown as a PDF, a good choice is using PrinceXML (you can also use Pandoc to do this). If you want to use PrinceXML then do the following:

	cd /opt/download
	wget http://www.princexml.com/download/prince-10r6-linux-generic-x86_64.tar.gz
	cd /opt
	tar -xvf download/prince-10r6-linux-generic-x86_64.tar.gz
	cd prince-10r6-linux-generic-x86_64/
	sudo ./install.sh

(And accept the defaults)

### Installing Pandoc (you want this)
Now let’s install Pandoc to be able to render Markdown to almost anything.

	cd /opt/download
	wget https://github.com/jgm/pandoc/releases/download/1.16.0.2/pandoc-1.16.0.2-1-amd64.deb
	ar p pandoc-1.16.0.2-1-amd64.deb data.tar.gz | sudo tar xvz --strip-components 2 -C /usr/local

### Setting up the Thoth configuration
We will now create the folders for use by Thoth (check the Thoth configuration file below to see the references to these folders):

	mkdir -p /data/thoth-workspace
	mkdir /opt/thoth-config

and then create the Thoth configuration file

	vi /opt/thoth-config/configuration.properties

and enter

\includecode{configuration.properties}

### Starting thoth for the first time
All is set up now, so we should be able to start HTTPD and then Tomcat. In case of problems have a look at the log file `catalina.out` in /opt/apache-tomcat-8.0.32/logs folder.

	service http start
	service tomcat start

When you fire a request with your browser Thoth will pull all your repositories (might take some time initially) and show you the index page.

If you are interested you can also check the contents of /data/thoth-workspace to see what Thoth has created (git repositories, search indexes, caches).

Note that it is safe to delete the contents of `/data/thoth-workspace` because Thoth will simply recreate the contents during startup. Just make sure Thoth is not running when you do this.