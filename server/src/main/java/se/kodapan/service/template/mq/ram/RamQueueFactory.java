package se.kodapan.service.template.mq.ram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import se.kodapan.service.template.mq.*;

import javax.inject.Singleton;

/**
 * @author kalle
 * @since 2017-03-07 21:15
 */
@Singleton
public class RamQueueFactory implements MessageQueueFactory {

  private RamMessageQueue ramMessageQueue = new RamMessageQueue();

  @Inject
  private ObjectMapper objectMapper;

  @Override
  public MessageQueueReader readerFactory(MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer) {
    return new RamQueueReader(ramMessageQueue, configuration, consumer, objectMapper);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new RamQueueWriter(ramMessageQueue);
  }

  @Override
  public long getQueueEndOffset(MessageQueueTopic topic) {
    throw new UnsupportedOperationException();
  }

}
