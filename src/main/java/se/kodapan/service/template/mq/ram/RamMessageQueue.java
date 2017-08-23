package se.kodapan.service.template.mq.ram;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.mq.MessageQueueMessage;
import se.kodapan.service.template.mq.MessageQueueReaderConfiguration;
import se.kodapan.service.template.mq.MessageQueueTopic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author kalle
 * @since 2017-02-15 08:45
 */
@Singleton
public class RamMessageQueue {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Map<MessageQueueTopic, Set<ConcurrentLinkedQueue<MessageQueueMessage>>> queuesByTopic = new HashMap<>();

  public Set<ConcurrentLinkedQueue<MessageQueueMessage>> getQueuesByTopic(MessageQueueTopic topic) {
    synchronized (queuesByTopic) {
      Set<ConcurrentLinkedQueue<MessageQueueMessage>> queues = queuesByTopic.get(topic);
      if (queues == null) {
        log.debug("Created queues set with topic '" + topic + "'");
        queues = new HashSet<>();
        queuesByTopic.put(topic, queues);
      }
      return queues;
    }
  }

  public void queueMessage(MessageQueueTopic topic, MessageQueueMessage message) {
    synchronized (queuesByTopic) {
      for (ConcurrentLinkedQueue<MessageQueueMessage> queue : getQueuesByTopic(topic)) {
        queue.add(message);
      }
    }
  }

  public ConcurrentLinkedQueue<MessageQueueMessage> registerQueue(MessageQueueReaderConfiguration configuration) {
    synchronized (queuesByTopic) {
      if (queuesByTopic.containsKey(configuration.getTopic())
          && !MessageQueueReaderConfiguration.AutoOffsetReset.latest.equals(configuration.getAutoOffsetReset())) {
        throw new UnsupportedOperationException("AutoOffsetReset must be latest");
      }
      // todo group
      ConcurrentLinkedQueue<MessageQueueMessage> queue = new ConcurrentLinkedQueue<>();
      getQueuesByTopic(configuration.getTopic()).add(queue);
      return queue;
    }
  }

  public void unregisterQueue(ConcurrentLinkedQueue<MessageQueueMessage> queue) {
    synchronized (queuesByTopic) {
      for (Set<ConcurrentLinkedQueue<MessageQueueMessage>> queues : queuesByTopic.values()) {
        queues.remove(queue);
      }
    }
  }
  
}
