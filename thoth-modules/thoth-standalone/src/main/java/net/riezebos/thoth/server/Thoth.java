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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ThothEnvironment;
import net.riezebos.thoth.exceptions.ContentManagerException;
import net.riezebos.thoth.server.commands.LogLevelServerCommand;
import net.riezebos.thoth.server.commands.PullServerCommand;
import net.riezebos.thoth.server.commands.ReindexServerCommand;
import net.riezebos.thoth.server.commands.ReloadServerCommand;
import net.riezebos.thoth.server.commands.ServerCommand;
import net.riezebos.thoth.servlets.ThothServlet;
import net.riezebos.thoth.util.ThothUtil;

/**
 * @author wido
 */
public class Thoth {

  private List<ServerCommand> serverCommands = new ArrayList<>();

  public void start(String args[]) throws Exception {
    ThothEnvironment thothEnvironment = new ThothEnvironment();
    ThothEnvironment.registerSharedContext(thothEnvironment);

    setupServerCommands(thothEnvironment);
    List<String> argumentsList = ThothUtil.getArgumentsList(args);
    Map<String, String> argumentsMap = ThothUtil.getArgumentsMap(args);
    boolean asServer = argumentsMap.containsKey("server");
    boolean initOnly = argumentsMap.containsKey("initonly");

    setupConfiguration(thothEnvironment, argumentsList);

    if (argumentsMap.containsKey("help"))
      printUsage();
    else {
      runServer(thothEnvironment, asServer, initOnly);
    }
  }

  protected void runServer(ThothEnvironment thothEnvironment, boolean asServer, boolean initOnly)
      throws Exception, ContentManagerException, InterruptedException, UnsupportedEncodingException, IOException {

    println("Thoth standalone v" + ThothUtil.getVersion(ThothUtil.Version.STANDALONE));

    if (initOnly)
      println("Initializing repositories. Please hang on...");
    else
      println("Server is firing up. Please hang on...");

    Configuration configuration = thothEnvironment.getConfiguration();
    Server server = new Server(configuration.getEmbeddedServerPort());
    ServerConnector httpConnector = new ServerConnector(server);
    httpConnector.setHost(configuration.getEmbeddedServerName());
    httpConnector.setIdleTimeout(configuration.getEmbeddedIdleTimeout() * 1000);
    server.addConnector(httpConnector);

    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(ThothServlet.class, "/");
    handler.initialize();

    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    context.setHandler(handler);

    HashSessionIdManager sessionIdManager = new HashSessionIdManager();
    server.setSessionIdManager(sessionIdManager);

    HashSessionManager sessionManager = new HashSessionManager();
    SessionHandler sessionHandler = new SessionHandler(sessionManager);

    sessionHandler.setHandler(context);

    server.setHandler(sessionHandler);

    println("Setting up content managers...");
    // Warm up the server
    thothEnvironment.touch();
    println("Content managers set up");

    if (!initOnly) {
      server.start();
      println("Thoth server started");
      println("You can now access Thoth at http://"//
          + configuration.getEmbeddedServerName() //
          + ":" + configuration.getEmbeddedServerPort());

      if (asServer) {
        server.join();
      } else {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

        println("Enter 'help' for a list of commands");

        boolean stop = false;
        do {
          stop = doCommand(thothEnvironment, configuration, br);
        } while (!stop);

        println("Stopping server...");
        server.stop();
        server.join();
      }
    }
    thothEnvironment.shutDown();
    println("Bye!");
  }

  protected boolean doCommand(ThothEnvironment thothEnvironment, Configuration configuration, BufferedReader br) {
    boolean stop = false;
    try {
      print("\nCommand: ");
      String line = br.readLine();
      line = line == null ? "" : line.trim();

      boolean handled = false;
      if ("help".equalsIgnoreCase(line)) {
        listCommands();
        handled = true;
      } else if ("stop".equalsIgnoreCase(line)) {
        stop = true;
        handled = true;
      } else {
        for (ServerCommand command : serverCommands) {
          if (command.applies(line)) {
            handled = true;
            if (!command.argumentsOk(line)) {
              println("Invalid argument(s)");
              listCommands();
            } else {
              command.execute(line);
            }
          }
        }
      }

      if (!handled && !StringUtils.isBlank(line)) {
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
    int indent = 18;
    println(ThothUtil.appendToLength("stop", " ", indent) + ": Stop the server");
    for (ServerCommand command : serverCommands) {
      println(ThothUtil.appendToLength(command.getUsage(), " ", indent) + ": " + command.getDescription());
    }
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

  protected void setupServerCommands(ThothEnvironment thothEnvironment) {
    serverCommands.add(new ReloadServerCommand(thothEnvironment));
    serverCommands.add(new PullServerCommand(thothEnvironment));
    serverCommands.add(new ReindexServerCommand(thothEnvironment));
    serverCommands.add(new LogLevelServerCommand(thothEnvironment));
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
    println("  -initonly: (Optional) only initialize (pull) the repositories, exit when done");
    println("  -server:   (Optional) run Thoth as a server. By default thoth will be interactive");
    println("  -help:     (Optional) show this message");
  }

  protected String validateConfigFile(String defaultConfigFileName) {
    String configurationFile = null;
    File file = new File(defaultConfigFileName);
    if (file.exists())
      configurationFile = file.getAbsolutePath();
    return configurationFile;
  }

  public static void main(String[] args) throws Exception {
    try {
      Thoth thoth = new Thoth();
      thoth.start(args);
    } catch (Exception e) {
      // Lets terminate when something bad happens during startup
      e.printStackTrace();
      System.exit(0);
    }
  }

}
