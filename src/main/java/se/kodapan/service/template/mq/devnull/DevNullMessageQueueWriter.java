package se.kodapan.service.template.mq.devnull;

import se.kodapan.service.template.mq.MessageQueueMessage;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.mq.MessageQueueWriter;

/**
 * @author kalle
 * @since 2017-03-22
 */
public class DevNullMessageQueueWriter implements MessageQueueWriter {

  @Override
  public boolean open() throws Exception {
    return true;
  }

  @Override
  public boolean close() throws Exception {
    return true;
  }

  @Override
  public void write(MessageQueueTopic topic, MessageQueueMessage message) throws Exception {

  }
}
