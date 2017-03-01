package se.kodapan.brfduva.service.template.mq.kafka;

import org.junit.Assert;
import org.junit.Test;
import se.kodapan.brfduva.service.template.mq.MessageQueueConsumer;
import se.kodapan.brfduva.service.template.mq.MessageQueueMessage;
import se.kodapan.brfduva.service.template.mq.MessageQueueTopic;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author kalle
 * @since 2017-02-23 01:04
 */
public class TestKafka {

  @Test
  public void test() throws Exception {

    long seed = System.currentTimeMillis();
    System.out.println("Using seed " + seed);
    Random random = new Random(seed);


    ConcurrentLinkedQueue<MessageQueueMessage> writtenMessages = new ConcurrentLinkedQueue<>();

    MessageQueueTopic topic = new MessageQueueTopic("test-" + System.currentTimeMillis(), "test");

    KafkaReader reader = new KafkaReader();
    Assert.assertTrue(reader.open());

    final List<MessageQueueMessage> consumed = new ArrayList<>();
    reader.registerConsumer(topic, new MessageQueueConsumer() {
      @Override
      public void consume(MessageQueueMessage message) {
        Assert.assertEquals(writtenMessages.poll(), message);
        consumed.add(message);
      }
    });


    KafkaWriter writer = new KafkaWriter();
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
