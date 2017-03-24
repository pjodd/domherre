package se.kodapan.service.template.mq.ram;

import se.kodapan.service.template.mq.*;

import javax.inject.Singleton;

/**
 * @author kalle
 * @since 2017-03-07 21:15
 */
@Singleton
public class RamQueueFactory implements MessageQueueFactory {

  private RamMessageQueue ramMessageQueue = new RamMessageQueue();

  @Override
  public MessageQueueReader readerFactory(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    return new RamQueueReader(ramMessageQueue, topic, consumer);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new RamQueueWriter(ramMessageQueue);
  }
}
