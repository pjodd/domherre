package se.kodapan.brfduva.service.template.mq.test;

import org.junit.Assert;
import org.junit.Test;
import se.kodapan.brfduva.service.template.mq.MessageQueueConsumer;
import se.kodapan.brfduva.service.template.mq.MessageQueueMessage;
import se.kodapan.brfduva.service.template.mq.MessageQueueTopic;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author kalle
 * @since 2017-02-15 09:01
 */
public class TestTestQueue {

  @Test
  public void test() throws Exception {

    ConcurrentLinkedQueue<MessageQueueMessage> writtenMessages = new ConcurrentLinkedQueue<>();

    MessageQueueTopic topic = new MessageQueueTopic("test", "test");

    TestQueue testQueue = new TestQueue();

    TestQueueReader reader = new TestQueueReader(testQueue);
    reader.subscribe(topic);
    reader.setConsumer(new MessageQueueConsumer() {
      @Override
      public void consume(MessageQueueMessage message) {
        Assert.assertEquals(writtenMessages.poll(), message);
      }
    });
    reader.open();

    Random random = new Random();

    TestQueueWriter writer = new TestQueueWriter(testQueue);
    for (int i = 0; i < 100; i++) {
//      System.out.println(i);

      Thread.sleep(random.nextInt(100 * (random.nextInt(10) == 0 ? 20 : 1)));

      MessageQueueMessage message = new MessageQueueMessage();
      message.setIdentity(UUID.randomUUID());
      message.setCreated(OffsetDateTime.now());
      message.setStereotype("Test event");
      message.setVersion(1);
      message.setPayload("payload");
    }

  }

}
