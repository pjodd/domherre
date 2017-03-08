package se.kodapan.brfduva.service.template.mq.test;

import se.kodapan.brfduva.service.template.mq.*;

/**
 * @author kalle
 * @since 2017-03-07 21:15
 */
public class TestQueueFactory implements MessageQueueFactory {

  private TestQueue testQueue = new TestQueue();

  @Override
  public MessageQueueReader readerFactory(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    return new TestQueueReader(testQueue, topic, consumer);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new TestQueueWriter(testQueue);
  }
}
