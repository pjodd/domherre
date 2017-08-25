package se.kodapan.service.template.mq.ram;

import se.kodapan.service.template.mq.MessageQueueMessage;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.mq.MessageQueueWriter;

/**
 * @author kalle
 * @since 2017-02-15 08:45
 */
public class RamQueueWriter implements MessageQueueWriter {

  private RamMessageQueue ramMessageQueue;

  public RamQueueWriter(RamMessageQueue ramMessageQueue) {
    this.ramMessageQueue = ramMessageQueue;
  }

  @Override
  public void write(MessageQueueTopic topic, MessageQueueMessage message) throws Exception {
    ramMessageQueue.queueMessage(topic, message);
  }

  @Override
  public boolean open() throws Exception {
    return ramMessageQueue != null;
  }

  @Override
  public boolean close() throws Exception {
    return true;
  }
}
