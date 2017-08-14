package net.riezebos.thoth.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.riezebos.thoth.configuration.Configuration;

public class ConsulUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ConsulUtil.class);

  public void initDefaultProperties(String consulUrl, String prefix, Properties props) throws IOException {
    HttpClientBuilder builder = HttpClientBuilder.create();
    HttpClient client = builder.build();

    for (Object obj : props.keySet()) {
      String key = String.valueOf(obj);
      String value = String.valueOf(props.get(obj));

      if (StringUtils.isBlank(getProperty(client, consulUrl, prefix, key)))
        setProperty(client, consulUrl, prefix, key, value);
    }
  }

  protected void setProperty(HttpClient client, String consulUrl, String prefix, String key, String value) throws IOException {
    String putUrl = ThothUtil.suffix(consulUrl, "/") + "v1/kv/" + prefix;
    putUrl = ThothUtil.suffix(putUrl, "/") + key.replace('.', '/');
    HttpPut request = new HttpPut(putUrl);

    HttpEntity entity = new StringEntity(value);
    request.setEntity(entity);
    HttpResponse response = client.execute(request);
    int statusCode = response.getStatusLine().getStatusCode();
    String result = ThothUtil.readInputStream(response.getEntity().getContent());
    if (statusCode != 200)
      LOG.error("Error setting {} because of {}", key, result);
    else
      LOG.debug("Set {} to {}", key, value);
  }

  protected String getProperty(HttpClient client, String consulUrl, String prefix, String key) throws IOException {
    String getUrl = ThothUtil.suffix(consulUrl, "/") + "v1/kv/" + prefix;
    getUrl = ThothUtil.suffix(getUrl, "/") + key.replace('.', '/') + "?raw";
    HttpGet request = new HttpGet(getUrl);

    HttpResponse response = client.execute(request);
    int statusCode = response.getStatusLine().getStatusCode();
    String result = ThothUtil.readInputStream(response.getEntity().getContent());
    if (statusCode == 404)
      return null;
    if (statusCode != 200)
      throw new IOException("Received " + statusCode + " for " + getUrl);
    System.out.println("Result for " + key + "=" + result);
    return result;
  }

  public static void main(String[] args) throws IOException {
    ConsulUtil cu = new ConsulUtil();
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    InputStream is = contextClassLoader.getResourceAsStream(Configuration.BUILTIN_PROPERTIES);
    Properties props = new Properties();
    props.load(is);

    cu.initDefaultProperties("http://localhost:8500", "thoth", props);
  }
}
