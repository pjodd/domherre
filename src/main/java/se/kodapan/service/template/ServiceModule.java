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
import com.google.inject.name.Names;
import org.gwizard.web.WebConfig;
import se.kodapan.service.template.mq.MessageQueueFactory;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.mq.kafka.KafkaFactory;

import javax.inject.Singleton;

/**
 * @author kalle
 * @since 2017-02-15 02:28
 */
public class ServiceModule implements Module {

  private String serviceName;
  private Class rootClass;

  public ServiceModule(String serviceName, Class rootClass) {
    this.serviceName = serviceName;
    this.rootClass = rootClass;
  }

  @Override
  public void configure(Binder binder) {
    binder.bind(MessageQueueFactory.class).annotatedWith(Names.named("prevalence journal factory")).to(KafkaFactory.class);
  }

  @Provides
  @Named("prevalence root")
  public Object rootFactory() throws Exception {
    return rootClass.newInstance();
  }

  @Provides
  @Named("service name")
  public String serviceNameFactory() {
    return serviceName;
  }

  @Singleton
  @Provides
  @Named("prevalence journal topic")
  public MessageQueueTopic prevalenceJournalTopicFactory() {
    return new MessageQueueTopic(serviceName, "prevalence-journal");
  }

  @Singleton
  @Provides
  public WebConfig webConfigFactory(){
    WebConfig webConfig = new WebConfig();
    webConfig.setPort(8080);
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
