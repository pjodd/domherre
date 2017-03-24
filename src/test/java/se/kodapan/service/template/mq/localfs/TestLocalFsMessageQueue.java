package se.kodapan.service.template.mq.localfs;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import junit.framework.Assert;
import org.junit.Test;
import se.kodapan.service.template.ServiceModule;
import se.kodapan.service.template.mq.*;
import se.kodapan.service.template.mq.ram.RamQueueFactory;
import se.kodapan.service.template.prevalence.TestMessageQueuePrevalence;

import java.io.File;

/**
 * @author kalle
 * @since 2017-03-24
 */
public class TestLocalFsMessageQueue {

  private MessageQueueTopic topic = new MessageQueueTopic("test", "event");

  @Test
  public void test() throws Exception {

    LocalFsMessageQueueFactory factory = new LocalFsMessageQueueFactory();
    factory.setAbsoluteRootPath("/tmp/localfsmq/" + System.currentTimeMillis());
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
    message.setPayload("test test " + System.currentTimeMillis());
    writer.write(topic, message);


    Assert.assertTrue(writer.close());
    Assert.assertTrue(reader.close());
    Assert.assertTrue(factory.close());
  }

}
