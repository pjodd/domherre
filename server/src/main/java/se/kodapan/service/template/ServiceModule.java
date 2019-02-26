package se.kodapan.service.template;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.gwizard.web.WebConfig;
import se.kodapan.service.template.util.Environment;
import se.kodapan.service.template.util.Tracking;

import javax.inject.Singleton;
import java.time.Clock;
import java.util.Collections;

/**
 * @author kalle
 * @since 2017-02-15 02:28
 */
public class ServiceModule extends AbstractServiceModule {

  public static final String SERVICE_NAME = "service name";

  public ServiceModule(String serviceName) {
    super(serviceName);
  }

  @Override
  public void configure(Binder binder) {
  }

  @Override
  @Provides
  @Named(SERVICE_NAME)
  public String getServiceName() {
    return super.getServiceName();
  }


  public int getServerPort() {
    return Environment.getValue("webConfig.port", 8080);
  }

  @Singleton
  @Provides
  public WebConfig webConfigFactory() {
    WebConfig webConfig = new WebConfig();
    webConfig.setPort(getServerPort());
    return webConfig;
  }

  @Provides
  @Singleton
  public ObjectMapper objectMapperFactory() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  @Provides
  @Singleton
  public Clock clockFactory() {
    return Clock.systemDefaultZone();
  }

  @Provides
  public CloseableHttpClient httpClientFactory() {
    return HttpClientBuilder.create()
        .setDefaultHeaders(Collections.singleton(new BasicHeader(Tracking.httpHeader, String.valueOf(Tracking.getInstance().get()))))
        .build();
  }

}
