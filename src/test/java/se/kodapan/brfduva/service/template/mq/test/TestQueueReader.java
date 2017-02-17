package se.kodapan.brfduva.service.template.mq.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.brfduva.service.template.mq.MessageQueueMessage;
import se.kodapan.brfduva.service.template.mq.MessageQueueReader;
import se.kodapan.brfduva.service.template.mq.MessageQueueTopic;
import se.kodapan.brfduva.service.template.mq.kafka.KafkaReader;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author kalle
 * @since 2017-02-15 08:45
 */
public class TestQueueReader extends MessageQueueReader {

  private Logger log = LoggerFactory.getLogger(getClass());

  private TestQueue testQueue;
  private MessageQueueTopic topic;

  private Poller poller;

  public TestQueueReader(TestQueue testQueue) {
    this.testQueue = testQueue;
  }

  @Override
  public boolean open() throws Exception {

    poller = new Poller();
    Thread pollerThread = new Thread(poller);
    pollerThread.setDaemon(true);
    pollerThread.setName("Test queue poller thread");
    pollerThread.start();

    return true;

  }

  @Override
  public boolean close() throws Exception {

    boolean success = true;

    if (poller != null) {
      poller.stopSignal.set(true);
      if (!poller.doneSignal.await(1, TimeUnit.MINUTES)) {
        log.error("Timed out waiting for poller to stop");
        success = false;
      } else {
        poller = null;
      }
    }
    return success;
  }

  @Override
  public void subscribe(MessageQueueTopic topic) {
    this.topic = topic;
  }


  private class Poller implements Runnable {

    private AtomicBoolean stopSignal;
    private CountDownLatch doneSignal;

    private int previousIndex = 0;

    @Override
    public void run() {
      stopSignal = new AtomicBoolean(false);
      doneSignal = new CountDownLatch(1);
      try {
        while (!stopSignal.get()) {
          try {
            List<MessageQueueMessage> queue = testQueue.getQueueByTopic(topic);
            if (queue.size() > previousIndex) {
              for (int index = previousIndex; index < queue.size(); previousIndex++) {
                MessageQueueMessage message = queue.get(index);
                try {
                  getConsumer().consume(message);
                } catch (Exception e) {
                  // todo
                }
                previousIndex = index;
              }
            }
          } catch (Exception e) {
            // todo

          } finally {

          }
        }
      } finally {
        doneSignal.countDown();
      }
    }

  }
}
