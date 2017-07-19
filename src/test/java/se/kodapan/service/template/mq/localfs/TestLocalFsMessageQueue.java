package se.kodapan.service.template.mq.localfs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import junit.framework.Assert;
import org.junit.Test;
import se.kodapan.service.template.ServiceModule;
import se.kodapan.service.template.mq.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2017-03-24
 */
public class TestLocalFsMessageQueue {

  private MessageQueueTopic topic = new MessageQueueTopic("test", "event");

  private ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void test() throws Exception {

    List<Module> modules = new ArrayList<>();
    modules.add(new ServiceModule("test"));
    modules.add(new LocalFsMessageQueueModule("/tmp/localfsmq/" + System.currentTimeMillis()));
    Injector injector = Guice.createInjector(modules);

    LocalFsMessageQueueFactory factory = injector.getInstance(LocalFsMessageQueueFactory.class);

    Assert.assertTrue(factory.open());

    MessageQueueReader reader = factory.readerFactory(topic, new MessageQueueConsumer() {
      @Override
      public void consume(MessageQueueMessage message) {
        System.out.println(message.toString());
      }
    });
    Assert.assertTrue(reader.open());


    MessageQueueWriter writer = factory.writerFactory();
    Assert.assertTrue(writer.open());
    MessageQueueMessage message = new MessageQueueMessage();
    message.setStereotype("stereotype");
    message.setVersion(1);
    message.setPayload(objectMapper.readValue("{\"epoch\": " + System.currentTimeMillis() + "}", JsonNode.class));
    writer.write(topic, message);


    Assert.assertTrue(writer.close());
    Assert.assertTrue(reader.close());
    Assert.assertTrue(factory.close());
  }

}
