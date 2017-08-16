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

import static se.kodapan.service.template.ServiceModule.SERVICE_NAME;

/**
 * @author kalle
 * @since 2017-07-19
 */
public class PrevalenceModule implements Module {

  public static final String PREVALENCE_ROOT = "prevalence root";

  public static final String PREVALENCE_JOURNAL_FACTORY = "prevalence journal factory";
  public static final String PREVALENCE_JOURNAL_TOPIC = "prevalence journal topic";

  private Class rootClass;

  private String serviceName;

  public PrevalenceModule(Class rootClass, String serviceName) {
    this.rootClass = rootClass;
    this.serviceName = serviceName;
  }

  @Override
  public void configure(Binder binder) {
    binder.bind(MessageQueueFactory.class).annotatedWith(Names.named(PREVALENCE_JOURNAL_FACTORY)).to(KafkaFactory.class);
  }

  @Provides
  @Named(PREVALENCE_ROOT)
  public Object rootFactory() throws Exception {
    return rootClass.newInstance();
  }

  @Singleton
  @Provides
  @Named(PREVALENCE_JOURNAL_TOPIC)
  public MessageQueueTopic prevalenceJournalTopicFactory() {
    return new MessageQueueTopic(serviceName, "prevalence-journal");
  }


}
