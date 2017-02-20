package se.kodapan.brfduva.service.template.mq.test;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.brfduva.service.template.mq.AbstractMessageQueueReader;
import se.kodapan.brfduva.service.template.mq.MessageQueueMessage;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author kalle
 * @since 2017-02-15 08:45
 */
public class TestQueueReader extends AbstractMessageQueueReader {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  @Setter
  @Getter
  private TestQueue testQueue;

  private Poller poller;

  @Override
  public boolean open() throws Exception {

    log.info("Opening test queue reader");

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


  private class Poller implements Runnable {

    private AtomicBoolean stopSignal;
    private CountDownLatch doneSignal;


    @Override
    public void run() {


      log.info("Starting reader poller thread");
      stopSignal = new AtomicBoolean(false);
      doneSignal = new CountDownLatch(1);
      try {
        while (!stopSignal.get()) {
          try {
            ConcurrentLinkedQueue<MessageQueueMessage> queue = testQueue.getQueueByTopic(getTopic());
            MessageQueueMessage message;
            while ((message = queue.poll()) != null) {
              try {
                getConsumer().consume(message);
              } catch (Exception e) {
                log.warn("Exception while consuming message\n" + message, e);
              }
            }
            Thread.sleep(1000);
          } catch (Exception e) {
            log.error("Caught exception", e);
          }
        }
      } finally {
        doneSignal.countDown();
      }
    }

  }
}
