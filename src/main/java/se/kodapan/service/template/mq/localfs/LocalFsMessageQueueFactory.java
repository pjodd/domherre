package se.kodapan.service.template.mq.localfs;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.kodapan.service.template.Initializable;
import se.kodapan.service.template.mq.*;

import java.io.File;

/**
 * @author kalle
 * @since 2017-03-24
 */
public class LocalFsMessageQueueFactory implements MessageQueueFactory, Initializable {

  @Inject
  @Named("local fs message queue absolute root path")
  private String absoluteRootPath;

  private LocalFsMessageQueue messageQueue;

  @Override
  public boolean open() throws Exception {
    messageQueue = new LocalFsMessageQueue(new File(absoluteRootPath));
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

  void setAbsoluteRootPath(String absoluteRootPath) {
    this.absoluteRootPath = absoluteRootPath;
  }
}


