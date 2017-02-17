package se.kodapan.brfduva.service.template.prevalence;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import junit.framework.Assert;
import lombok.Data;
import org.junit.Test;
import se.kodapan.brfduva.service.template.ServiceModule;
import se.kodapan.brfduva.service.template.mq.MessageQueueReader;
import se.kodapan.brfduva.service.template.mq.MessageQueueWriter;
import se.kodapan.brfduva.service.template.mq.test.TestQueueReader;
import se.kodapan.brfduva.service.template.mq.test.TestQueueWriter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kalle
 * @since 2017-02-17 15:45
 */
public class TestPrevalence {

  @Test
  public void test() throws Exception {

    Injector injector = Guice.createInjector(new ServiceModule("test", Root.class) {

      @Override
      public void configure(Binder binder) {
        binder.bind(MessageQueueWriter.class).to(TestQueueWriter.class);
        binder.bind(MessageQueueReader.class).to(TestQueueReader.class);
        binder.bind(Prevalence.class).to(new TypeLiteral<Prevalence<Root>>(){});
      }

    });

    Prevalence<Root> prevalence = injector.getInstance(Prevalence.class);
    MessageQueuePrevalence<Root> messageQueuePrevalence = injector.getInstance(MessageQueuePrevalence.class);
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
