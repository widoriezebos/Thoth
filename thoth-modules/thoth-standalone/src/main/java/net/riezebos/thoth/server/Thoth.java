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
import java.io.InputStreamReader;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

import net.riezebos.thoth.configuration.Configuration;
import net.riezebos.thoth.configuration.ConfigurationFactory;
import net.riezebos.thoth.content.ContentManagerFactory;
import net.riezebos.thoth.servlets.ThothServlet;
import net.riezebos.thoth.util.ThothUtil;

/**
 * @author wido
 */
public class Thoth {

  public void start(String args[]) throws Exception {
    System.out.println("Thoth standalone v" + ThothUtil.getVersion(ThothUtil.Version.STANDALONE));
    System.out.println("Server is firing up. Please hang on...");
    String configurationFile = ConfigurationFactory.determinePropertyPath();

    if (args.length > 0) {
      configurationFile = tryConfigFile(args[0]);
    }
    if (configurationFile == null) {
      configurationFile = tryConfigFile("configuration.properties");
    }

    if (configurationFile == null) {
      String message = "No Configuration found. Please specify a configuration file to use either by\n"//
          + "1) Passing it as a command line argument i.e. 'java " + Thoth.class.getName() + " /my/own/configuration.properties'\n"//
          + "2) Setting an environment variable i.e. set " + ConfigurationFactory.CONFIGKEY + "=/my/own/configuration.properties\n"//
          + "3) Passing a System variable to the Java VM i.e. -D" + ConfigurationFactory.CONFIGKEY + "=/my/own/configuration.properties";
      throw new IllegalArgumentException(message);
    }
    File check = new File(configurationFile);
    if (!check.exists())
      throw new FileNotFoundException("Configuration file " + configurationFile + " not found");

    System.setProperty(ConfigurationFactory.CONFIGKEY, configurationFile);

    Configuration configuration = ConfigurationFactory.getConfiguration();

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

    System.out.println("Setting up content managers...");
    // Warm up the server
    ContentManagerFactory.touch();

    server.start();
    System.out.println("Thoth server started.\n"//
        + "You can now access Thoth at http://"//
        + configuration.getEmbeddedServerName() //
        + ":" + configuration.getEmbeddedServerPort());
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

    boolean stop = false;
    do {
      System.out.println("Just enter 'stop' to stop the server");
      String line = br.readLine();
      stop = "stop".equalsIgnoreCase(line);
      if (!stop)
        System.out.println("Did not recognize command. Please reenter.");
    } while (!stop);

    System.out.println("Stopping server.\n(First waiting for any auto refresh to finish though)");
    server.stop();
    server.join();
    ContentManagerFactory.shutDown();
  }

  protected String tryConfigFile(String defaultConfigFileName) {
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
