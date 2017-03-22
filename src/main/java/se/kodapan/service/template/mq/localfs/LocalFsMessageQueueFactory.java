package se.kodapan.service.template.mq.localfs;

import se.kodapan.service.template.Initializable;
import se.kodapan.service.template.mq.*;

import java.io.File;

/**
 * Created by kalle on 2017-03-22.
 */
public class LocalFsMessageQueueFactory implements MessageQueueFactory, Initializable {

  private LocalFsMessageQueue messageQueue;

  @Override
  public boolean open() throws Exception {
    messageQueue = new LocalFsMessageQueue();
    // todo
    return true;
  }

  @Override
  public boolean close() throws Exception {
    return true;
  }

  @Override
  public MessageQueueReader readerFactory(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    return new LocalFsMessageQueueReader(messageQueue, topic, consumer);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new LocalFsMessageQueueWriter(messageQueue);
  }
}
