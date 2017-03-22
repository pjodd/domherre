package se.kodapan.service.template.mq.localfs;

import se.kodapan.service.template.mq.MessageQueueMessage;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.mq.MessageQueueWriter;

/**
 * Created by kalle on 2017-03-22.
 */
public class LocalFsMessageQueueWriter implements MessageQueueWriter {

  private LocalFsMessageQueue queue;

  public LocalFsMessageQueueWriter(LocalFsMessageQueue queue) {
    this.queue = queue;
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
  public void write(MessageQueueTopic topic, MessageQueueMessage message) throws Exception {
    // todo
  }
}
