package se.kodapan.service.template.mq.localfs;

import se.kodapan.service.template.mq.MessageQueueConsumer;
import se.kodapan.service.template.mq.MessageQueueReader;
import se.kodapan.service.template.mq.MessageQueueReaderConfiguration;
import se.kodapan.service.template.mq.MessageQueueTopic;

/**
 * @author kalle
 * @since 2017-03-24
 */
public class LocalFsMessageQueueReader implements MessageQueueReader {

  private LocalFsMessageQueue queue;
  private MessageQueueReaderConfiguration configuration;
  private MessageQueueConsumer consumer;

  public LocalFsMessageQueueReader(LocalFsMessageQueue queue, MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer) {
    this.queue = queue;
    this.configuration = configuration;
    this.consumer = consumer;
  }

  @Override
  public boolean open() throws Exception {
    queue.register(this);
    return true;
  }

  @Override
  public boolean close() throws Exception {
    queue.unregister(this);
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
