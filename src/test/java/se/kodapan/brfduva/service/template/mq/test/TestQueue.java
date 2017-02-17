package se.kodapan.brfduva.service.template.mq.test;

import com.google.inject.Singleton;
import lombok.Data;
import se.kodapan.brfduva.service.template.mq.MessageQueueMessage;
import se.kodapan.brfduva.service.template.mq.MessageQueueTopic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kalle
 * @since 2017-02-15 08:45
 */
@Singleton
public class TestQueue {

  private Map<MessageQueueTopic, List<MessageQueueMessage>> queueByTopic = new HashMap<>();

  public synchronized List<MessageQueueMessage> getQueueByTopic(MessageQueueTopic topic) {
    List<MessageQueueMessage> queue = queueByTopic.get(topic);
    if (queue == null) {
      queue = new ArrayList<>();
      queueByTopic.put(topic, queue);
    }
    return queue;
  }

}
