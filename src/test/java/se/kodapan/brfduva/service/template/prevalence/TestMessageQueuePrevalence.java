package se.kodapan.brfduva.service.template.prevalence;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import junit.framework.Assert;
import lombok.Data;
import org.junit.Test;
import se.kodapan.brfduva.service.template.ServiceModule;
import se.kodapan.brfduva.service.template.mq.MessageQueueFactory;
import se.kodapan.brfduva.service.template.mq.MessageQueueReader;
import se.kodapan.brfduva.service.template.mq.MessageQueueWriter;
import se.kodapan.brfduva.service.template.mq.kafka.KafkaFactory;
import se.kodapan.brfduva.service.template.mq.test.TestQueueFactory;
import se.kodapan.brfduva.service.template.mq.test.TestQueueReader;
import se.kodapan.brfduva.service.template.mq.test.TestQueueWriter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kalle
 * @since 2017-02-17 15:45
 */
public class TestMessageQueuePrevalence {

  @Test
  public void test() throws Exception {

    Injector injector = Guice.createInjector(new ServiceModule("test", Root.class) {

      @Override
      public void configure(Binder binder) {
        binder.bind(MessageQueueFactory.class).annotatedWith(Names.named("prevalence journal factory")).to(TestQueueFactory.class);
      }

    });

    Prevalence prevalence = injector.getInstance(Prevalence.class);
    MessageQueuePrevalence messageQueuePrevalence = injector.getInstance(MessageQueuePrevalence.class);
    Assert.assertTrue(messageQueuePrevalence.open());

    Assert.assertEquals(2, messageQueuePrevalence.execute(TestTransaction.class, new Payload(2)).getSum());
    Assert.assertEquals(3, messageQueuePrevalence.execute(TestTransaction.class, new Payload(1)).getSum());
    
    System.currentTimeMillis();



  }

  @Data
  public static class Root {
    private AtomicInteger counter = new AtomicInteger(0);
  }

  @Data
  public static class Payload {
    private int increment;

    public Payload() {
    }

    public Payload(int increment) {
      this.increment = increment;
    }
  }

  @Data
  public static class Response {
    private int sum;
  }

  public static class TestTransaction implements Transaction<Root, Payload, Response> {
    @Override
    public String getStereotype() {
      return "TestTransaction";
    }

    @Override
    public Integer getVersion() {
      return 1;
    }

    @Override
    public Class<Payload> getPayloadClass() {
      return Payload.class;
    }

    @Override
    public Class<Response> getResponseClass() {
      return Response.class;
    }

    @Override
    public Response execute(Root root, Payload payload) throws Exception {
      Response response = new Response();
      response.setSum(root.getCounter().addAndGet(payload.getIncrement()));
      return response;
    }
  }

}
