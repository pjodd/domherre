package se.kodapan.service.template.mq.localfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.kodapan.service.template.Initializable;
import se.kodapan.service.template.mq.*;

import javax.inject.Singleton;
import java.io.File;

/**
 * @author kalle
 * @since 2017-03-24
 */
@Singleton
public class LocalFsMessageQueueFactory implements MessageQueueFactory, Initializable {

  @Inject
  @Named("local fs message queue absolute root path")
  private String absoluteRootPath;

  private LocalFsMessageQueue messageQueue;

  @Inject
  private ObjectMapper objectMapper;

  @Override
  public boolean open() throws Exception {
    messageQueue = new LocalFsMessageQueue(new File(absoluteRootPath), objectMapper);
    return true;
  }

  @Override
  public boolean close() throws Exception {
    return true;
  }

  @Override
  public MessageQueueReader readerFactory(MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer) {
    return new LocalFsMessageQueueReader(messageQueue, configuration, consumer);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new LocalFsMessageQueueWriter(messageQueue);
  }

}


