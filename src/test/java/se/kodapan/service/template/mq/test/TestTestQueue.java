package se.kodapan.service.template.mq.test;

import org.junit.Assert;
import org.junit.Test;
import se.kodapan.service.template.mq.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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

    long seed = System.currentTimeMillis();
    System.out.println("Using seed " + seed);
    Random random = new Random(seed);


    ConcurrentLinkedQueue<MessageQueueMessage> writtenMessages = new ConcurrentLinkedQueue<>();

    MessageQueueTopic topic = new MessageQueueTopic("test-" + System.currentTimeMillis(), "test");

    TestQueueFactory factory= new TestQueueFactory();

    final List<MessageQueueMessage> consumed = new ArrayList<>();
    MessageQueueReader reader = factory.readerFactory(topic, new MessageQueueConsumer() {
      @Override
      public void consume(MessageQueueMessage message) {
        Assert.assertEquals(writtenMessages.poll(), message);
        consumed.add(message);
      }
    });
    Assert.assertTrue(reader.open());

    MessageQueueWriter writer = factory.writerFactory();
    Assert.assertTrue(writer.open());

    for (int i = 0; i < 100; i++) {
      System.out.println(i);

      Thread.sleep(random.nextInt(100 * (random.nextInt(10) == 0 ? 20 : 1)));

      MessageQueueMessage message = new MessageQueueMessage();
      message.setIdentity(UUID.randomUUID());
      message.setCreated(OffsetDateTime.now());
      message.setStereotype("Test event");
      message.setVersion(1);
      message.setPayload("{ \"key\": \"value\"}");

      writtenMessages.add(message);
      writer.write(topic, message);
    }

    Thread.sleep(5000);
    Assert.assertEquals(100, consumed.size());

  }

}
