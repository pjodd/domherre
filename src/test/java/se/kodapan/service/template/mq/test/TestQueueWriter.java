package se.kodapan.service.template.mq.test;

import se.kodapan.service.template.mq.MessageQueueMessage;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.mq.MessageQueueWriter;

/**
 * @author kalle
 * @since 2017-02-15 08:45
 */
public class TestQueueWriter implements MessageQueueWriter {

  private TestQueue testQueue;

  public TestQueueWriter(TestQueue testQueue) {
    this.testQueue = testQueue;
  }

  @Override
  public void write(MessageQueueTopic topic, MessageQueueMessage message) throws Exception {
    testQueue.getQueueByTopic(topic).add(message);
  }

  @Override
  public boolean open() throws Exception {
    return testQueue != null;
  }

  @Override
  public boolean close() throws Exception {
    return true;
  }
}
