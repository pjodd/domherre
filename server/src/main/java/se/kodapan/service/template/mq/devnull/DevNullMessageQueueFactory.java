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
  public MessageQueueReader readerFactory(MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer) {
    return new DevNullMessageQueueReader(configuration, consumer);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new DevNullMessageQueueWriter();
  }

  @Override
  public long getQueueEndOffset(MessageQueueTopic topic) {
    throw new UnsupportedOperationException();
  }
}
