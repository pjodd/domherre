package se.kodapan.brfduva.service.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import se.kodapan.brfduva.service.template.mq.MessageQueueTopic;

import java.util.Collection;
import java.util.Collections;

/**
 * @author kalle
 * @since 2017-02-15 02:28
 */
public abstract class ServiceModule implements Module {

  private String serviceName;
  private Class rootClass;

  public ServiceModule(String serviceName, Class rootClass) {
    this.serviceName = serviceName;
    this.rootClass = rootClass;
  }

  @Override
  public void configure(Binder binder) {

  }

  @Provides
  @Named("root")
  public Object rootProvider() throws Exception {
    return rootClass.newInstance();

  }

  @Provides
  @Named("service name")
  public String serviceNameProvider() {
    return serviceName;
  }

  @Provides
  @Named("prevalence journal topic")
  public MessageQueueTopic prevalenceJournalTopicProvider() {
    return new MessageQueueTopic(serviceName, "prevalence-journal");

  }

  @Provides
  public ObjectMapper objectMapperFactory() {
    // todo dates and what not
    return new ObjectMapper();
  }

}
