package se.kodapan.service.template.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author kalle
 * @since 2017-09-28 19:11
 */
public class RestClient implements Closeable {

  @Inject
  private ObjectMapper objectMapper;

  @Inject
  private CloseableHttpClient httpClient;

  private String hostName;
  private int port;

  public RestClient(String hostName, int port) {
    this.hostName = hostName;
    this.port = port;
  }

  private StringBuilder urlFactory() {
    return new StringBuilder(128).append("http://").append(hostName).append(":").append(String.valueOf(port)).append("://api/");
  }

  protected CloseableHttpResponse get(String path) throws Exception {
    return httpClient.execute(new HttpGet(urlFactory().append(path).toString()));
  }

  protected CloseableHttpResponse put(String path, Object object) throws Exception {
    return put(path, objectMapper.writeValueAsString(object));
  }

  protected CloseableHttpResponse put(String path, String json) throws Exception {
    HttpPut put = new HttpPut(urlFactory().append(path).toString());
    put.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
    return httpClient.execute(put);
  }

  protected CloseableHttpResponse post(String path, Object object) throws Exception {
    return post(path, objectMapper.writeValueAsString(object));
  }

  protected CloseableHttpResponse post(String path, String json) throws Exception {
    HttpPost post = new HttpPost(urlFactory().append(path).toString());
    post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
    return httpClient.execute(post);
  }

  protected CloseableHttpResponse delete(String path) throws Exception {
    return httpClient.execute(new HttpDelete(urlFactory().append(path).toString()));
  }

  protected <R> R parse(CloseableHttpResponse response, Class<R> responseClass) throws IOException {
    try {
      if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 299) {
        return objectMapper.readValue(response.getEntity().getContent(), responseClass);
      } else {
        throw new IOException("HTTP " + response.getStatusLine().getStatusCode());
      }
    } finally {
      response.close();
    }
  }

  @Override
  public void close() throws IOException {
    httpClient.close();
  }


}
