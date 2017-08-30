package se.kodapan.service.template.mq.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.Assert;
import org.junit.Test;
import se.kodapan.service.template.ServiceModule;
import se.kodapan.service.template.ServiceTest;
import se.kodapan.service.template.mq.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Just ensures that we can send and read messages sent on Kafka.
 * Has nothing to do with Prevalence.
 *
 * @author kalle
 * @since 2017-02-23 01:04
 */
public class TestKafka extends ServiceTest {


  @Test
  public void test() throws Exception {

    long seed = System.currentTimeMillis();
    System.out.println("Using seed " + seed);
    Random random = new Random(seed);

    List<Module> modules = new ArrayList<>();
    modules.add(new ServiceModule("test"));
    Injector injector = Guice.createInjector(modules);

    ObjectMapper objectMapper = injector.getInstance(ObjectMapper.class);
    KafkaFactory factory = injector.getInstance(KafkaFactory.class);


    ConcurrentLinkedQueue<MessageQueueMessage> writtenMessages = new ConcurrentLinkedQueue<>();

    MessageQueueTopic topic = new MessageQueueTopic("test-" + System.currentTimeMillis(), "test");


    final List<MessageQueueMessage> consumed = new ArrayList<>();

    MessageQueueReader reader = factory.readerFactory(
        new MessageQueueReaderConfiguration(topic),
        new MessageQueueConsumer() {
          @Override
          public void consume(MessageQueueMessage message, MessageQueueConsumerContext context) {
            Assert.assertTrue(TestKafka.equals(writtenMessages.poll(), message));
            consumed.add(message);
            System.out.println("in: " + message.toString());
          }
        });
    Assert.assertTrue(reader.open());

    MessageQueueWriter writer = factory.writerFactory();
    Assert.assertTrue(writer.open());

    for (int i = 0; i < 100; i++) {

      int sleep = random.nextInt(100 * (random.nextInt(10) == 0 ? 20 : 1));
      System.out.println("sleep: " + sleep);
      Thread.sleep(sleep);

      MessageQueueMessage message = new MessageQueueMessage();
      message.setIdentity(UUID.randomUUID());
      message.setCreated(OffsetDateTime.now());
      message.setStereotype("Test event");
      message.setVersion(1);
      message.setPayload(objectMapper.readValue("{ \"key\": \"value\"}", JsonNode.class));

      writtenMessages.add(message);
      writer.write(topic, message);

      System.out.println("out: " + message.toString());

    }

    Thread.sleep(5000);
    Assert.assertEquals(100, consumed.size());


  }

  public static boolean equals(MessageQueueMessage m1, MessageQueueMessage m2) {
    if (m1 == null && m2 == null) {
      return true;
    }
    if (m1 == null && m2 != null) {
      return false;
    }
    if (m1 != null && m2 == null) {
      return false;
    }
    if (!(m1.getCreated().toInstant().equals(m2.getCreated().toInstant())
        && m1.getIdentity().equals(m2.getIdentity())
        && m1.getStereotype().equals(m2.getStereotype())
        && m1.getVersion() == m2.getVersion())
        ) {
      return false;
    }
    if (m1.getPayload() == null && m2.getPayload() == null) {
      return true;
    }
    if (m1.getPayload() == null && m2.getPayload() != null) {
      return false;
    }
    if (m1.getPayload() != null && m2.getPayload() == null) {
      return false;
    }
    return m1.getPayload().equals(m2.getPayload());
  }

}
