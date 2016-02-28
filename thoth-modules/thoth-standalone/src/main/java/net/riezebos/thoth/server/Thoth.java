/* Copyright (c) 2016 W.T.J. Riezebos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.riezebos.thoth.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.servlets.ThothServlet;
import net.riezebos.thoth.util.ThothUtil;

/**
 * @author wido
 */
public class Thoth {

  public void start(String args[]) throws Exception {
    ThothEnvironment thothEnvironment = new ThothEnvironment();
    ThothEnvironment.registerSharedContext(thothEnvironment);

    List<String> argumentsList = ThothUtil.getArgumentsList(args);
    Map<String, String> argumentsMap = ThothUtil.getArgumentsMap(args);
    boolean asServer = argumentsMap.containsKey("server");

    setupConfiguration(thothEnvironment, argumentsList);

    if (argumentsMap.containsKey("help"))
      printUsage();
    else {
      runServer(thothEnvironment, asServer);
    }
  }

  protected void runServer(ThothEnvironment thothEnvironment, boolean asServer)
      throws Exception, ContentManagerException, InterruptedException, UnsupportedEncodingException, IOException {

    println("Thoth standalone v" + ThothUtil.getVersion(ThothUtil.Version.STANDALONE));
    println("Server is firing up. Please hang on...");

    Configuration configuration = thothEnvironment.getConfiguration();
    Server server = new Server(8080);
    ServerConnector httpConnector = new ServerConnector(server);
    httpConnector.setHost(configuration.getEmbeddedServerName());
    httpConnector.setPort(configuration.getEmbeddedServerPort());
    httpConnector.setIdleTimeout(configuration.getEmbeddedIdleTimeout() * 1000);
    server.addConnector(httpConnector);

    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(ThothServlet.class, "/");
    handler.initialize();

    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    context.setHandler(handler);

    server.setHandler(context);

    println("Setting up content managers...");
    // Warm up the server
    thothEnvironment.touch();

    server.start();
    println("Thoth server started.");

    if (asServer) {
      server.join();
    } else {
      println("You can now access Thoth at http://"//
          + configuration.getEmbeddedServerName() //
          + ":" + configuration.getEmbeddedServerPort());
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

      listCommands();

      boolean stop = false;
      do {
        stop = doCommand(thothEnvironment, configuration, br);
      } while (!stop);

      println("Stopping server...");
      server.stop();
      server.join();
      thothEnvironment.shutDown();
    }
  }

  protected boolean doCommand(ThothEnvironment thothEnvironment, Configuration configuration, BufferedReader br) {
    boolean stop = false;
    try {
      print("Command: ");
      String line = br.readLine().trim();

      if ("reload".equalsIgnoreCase(line)) {
        configuration.reload();
        println("Configuration reloaded from " + configuration.getPropertyFileName());

      } else if ("pull".equalsIgnoreCase(line)) {
        println("Pulling...");
        thothEnvironment.pullAll();
        println("Done...");

      } else if ("reindex".equalsIgnoreCase(line)) {
        println("Reindex running in the background");
        thothEnvironment.reindexAll();

      } else if ("stop".equalsIgnoreCase(line)) {
        stop = true;

      } else if ("debug".equalsIgnoreCase(line)) {
        LogManager.getRootLogger().setLevel(Level.DEBUG);
        println("Logger level set to DEBUG");

      } else if ("info".equalsIgnoreCase(line)) {
        LogManager.getRootLogger().setLevel(Level.INFO);
        println("Logger level set to INFO");

      } else if ("warn".equalsIgnoreCase(line)) {
        println("Logger level set to WARN");
        LogManager.getRootLogger().setLevel(Level.WARN);

      } else if (!StringUtils.isBlank(line)) {
        println("\nDid not recognize command '" + line + "'");
        listCommands();
      }
    } catch (Exception e) {
      print(e.getMessage());
    }
    return stop;
  }

  protected void listCommands() {
    println("\nThe following console commands are supported:");
    println("reload:  Reload the configuration");
    println("pull:    Pull all version controlled repositories");
    println("reindex: Force a reindex of all repositories");
    println("debug:   Switch to debug log level");
    println("info:    Switch to info log level");
    println("warn:    Switch to warn log level");
    println("stop:    Stop the server");
  }

  protected void setupConfiguration(ThothEnvironment thothEnvironment, List<String> argumentsList) throws FileNotFoundException {

    String configurationFile = thothEnvironment.determinePropertyPath();
    if (!argumentsList.isEmpty()) {
      configurationFile = validateConfigFile(argumentsList.get(0));
    }
    if (configurationFile == null) {
      configurationFile = validateConfigFile("configuration.properties");
    }

    if (configurationFile == null) {
      String message = "No Configuration found. Please specify a configuration file to use either by\n"//
          + "1) Passing it as a command line argument i.e. 'java " + Thoth.class.getName() + " /my/own/configuration.properties'\n"//
          + "2) Setting an environment variable i.e. set " + ThothEnvironment.CONFIGKEY + "=/my/own/configuration.properties\n"//
          + "3) Passing a System variable to the Java VM i.e. -D" + ThothEnvironment.CONFIGKEY + "=/my/own/configuration.properties";
      throw new IllegalArgumentException(message);
    }
    File check = new File(configurationFile);
    if (!check.exists())
      throw new FileNotFoundException("Configuration file " + configurationFile + " not found");

    System.setProperty(ThothEnvironment.CONFIGKEY, configurationFile);
  }

  protected void println(String message) {
    System.out.println(message);
  }

  protected void print(String message) {
    System.out.print(message);
  }

  protected void printUsage() {
    String version = ThothUtil.getVersion(ThothUtil.Version.STANDALONE);
    println("Thoth standalone v" + version);
    println("Usage: java -jar thoth-standalone-" + version + ".jar [configfilename] [-server] [-help]\n");
    println("Arguments:");
    println("  configfilename: (Optional)");
    println("           The path to the configuration file. If not given then Thoth");
    println("           will use the environment variable thoth_configuration or");
    println("           the system variable thoth_configuration.");
    println("           As a last resort Thoth will try the working folder for a file");
    println("           named 'configuration.properties'");
    println("Flags:");
    println("  -server: (Optional) run Thoth as a server. By default thoth will be interactive");
    println("  -help:   (Optional) show this message");
  }

  protected String validateConfigFile(String defaultConfigFileName) {
    String configurationFile = null;
    File file = new File(defaultConfigFileName);
    if (file.exists())
      configurationFile = file.getAbsolutePath();
    return configurationFile;
  }

  public static void main(String[] args) throws Exception {
    Thoth thoth = new Thoth();
    thoth.start(args);
  }

}
