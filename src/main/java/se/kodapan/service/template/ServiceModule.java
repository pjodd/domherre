package se.kodapan.service.template;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.gwizard.web.WebConfig;

import javax.inject.Singleton;

/**
 * @author kalle
 * @since 2017-02-15 02:28
 */
public class ServiceModule implements Module {

  public static final String SERVICE_NAME = "service name";

  private String serviceName;

  public ServiceModule(String serviceName) {
    this.serviceName = serviceName;
  }

  @Override
  public void configure(Binder binder) {
  }

  @Provides
  @Named(SERVICE_NAME)
  public String serviceNameFactory() {
    return serviceName;
  }


  public int getServerPort() {
    return 8080;
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

}
