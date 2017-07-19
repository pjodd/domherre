package se.kodapan.service.template.prevalence;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import se.kodapan.service.template.mq.MessageQueueFactory;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.mq.kafka.KafkaFactory;

import javax.inject.Singleton;

/**
 * @author kalle
 * @since 2017-07-19
 */
public class PrevalenceModule implements Module {

  private Class rootClass;

  @Inject
  @Named("service name")
  private String serviceName;

  public PrevalenceModule(Class rootClass) {
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

  @Singleton
  @Provides
  @Named("prevalence journal topic")
  public MessageQueueTopic prevalenceJournalTopicFactory() {
    return new MessageQueueTopic(serviceName, "prevalence-journal");
  }


}
