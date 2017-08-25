package se.kodapan.service.template.mq.devnull;

import se.kodapan.service.template.mq.MessageQueueConsumer;
import se.kodapan.service.template.mq.MessageQueueReader;
import se.kodapan.service.template.mq.MessageQueueReaderConfiguration;
import se.kodapan.service.template.mq.MessageQueueTopic;

/**
 * @author kalle
 * @since 2017-03-22
 */
public class DevNullMessageQueueReader implements MessageQueueReader {

  private MessageQueueReaderConfiguration configuration;
  private MessageQueueConsumer consumer;

  public DevNullMessageQueueReader(MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer) {
    this.configuration = configuration;
    this.consumer = consumer;
  }

  @Override
  public boolean open() throws Exception {
    return true;
  }

  @Override
  public boolean close() throws Exception {
    return true;
  }

  @Override
  public MessageQueueReaderConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public MessageQueueConsumer getConsumer() {
    return consumer;
  }
}
