package se.kodapan.service.template.mq.localfs;

import lombok.Data;
import se.kodapan.service.template.mq.MessageQueueMessage;
import se.kodapan.service.template.mq.MessageQueueTopic;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * @author kalle
 * @since 2017-03-24
 */
@Data
public class LocalFsMessageQueue {

  private File rootPath;

  public LocalFsMessageQueue(File rootPath) {
    this.rootPath = rootPath;
  }

  private Map<MessageQueueTopic, LocalFsMessageQueueTopic> topics = new HashMap<>();


  void register(LocalFsMessageQueueReader reader) throws Exception {
    LocalFsMessageQueueTopic topic = getTopic(reader.getTopic());
    topic.register(reader);
  }

  LocalFsMessageQueueTopic getTopic(MessageQueueTopic topic) throws IOException {
    synchronized (topics) {
      LocalFsMessageQueueTopic lfstopic = this.topics.get(topic);
      if (lfstopic == null) {
        lfstopic = new LocalFsMessageQueueTopic(this, topic);
        lfstopic.open();
        topics.put(topic, lfstopic);
      }
      return lfstopic;
    }
  }

  void unregister(LocalFsMessageQueueReader reader) {
    synchronized (topics) {
      LocalFsMessageQueueTopic topic = this.topics.get(reader.getTopic());
      if (topic != null) {
        topic.unregister(reader);
      }
    }
  }

}
