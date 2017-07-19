package se.kodapan.service.template.mq.devnull;

import se.kodapan.service.template.mq.*;

import javax.inject.Singleton;

/**
 * @author kalle
 * @since 2017-03-22
 */
@Singleton
public class DevNullMessageQueueFactory implements MessageQueueFactory {

  @Override
  public MessageQueueReader readerFactory(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    return new DevNullMessageQueueReader(topic, consumer);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new DevNullMessageQueueWriter();
  }
}
