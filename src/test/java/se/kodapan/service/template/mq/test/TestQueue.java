package se.kodapan.service.template.mq.test;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.mq.MessageQueueMessage;
import se.kodapan.service.template.mq.MessageQueueTopic;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author kalle
 * @since 2017-02-15 08:45
 */
@Singleton
public class TestQueue {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Map<MessageQueueTopic, ConcurrentLinkedQueue<MessageQueueMessage>> queueByTopic = new HashMap<>();

  public synchronized ConcurrentLinkedQueue<MessageQueueMessage> getQueueByTopic(MessageQueueTopic topic) {
    ConcurrentLinkedQueue<MessageQueueMessage> queue = queueByTopic.get(topic);
    if (queue == null) {
      log.debug("Created queue with topic '" + topic + "'");
      queue = new ConcurrentLinkedQueue<>();
      queueByTopic.put(topic, queue);
    }
    return queue;
  }

}
