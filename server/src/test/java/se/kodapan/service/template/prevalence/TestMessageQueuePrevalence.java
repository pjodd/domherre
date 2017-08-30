package se.kodapan.service.template.prevalence;

import com.google.inject.Module;
import junit.framework.Assert;
import lombok.Data;
import org.junit.Test;
import se.kodapan.service.template.Initializable;
import se.kodapan.service.template.Service;
import se.kodapan.service.template.ServiceTest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kalle
 * @since 2017-02-17 15:45
 */
public class TestMessageQueuePrevalence extends ServiceTest {

  @Test
  public void test() throws Exception {

    String serviceName = "test-" + System.currentTimeMillis();

    // first run, execute a bunch of transactions.

    Service service = serviceFactory(serviceName);

    Assert.assertTrue(service.open());
    try {
      
      Prevalence prevalence = service.getInjector().getInstance(Prevalence.class);
      MessageQueuePrevalence messageQueuePrevalence = service.getInjector().getInstance(MessageQueuePrevalence.class);

      Assert.assertEquals(2, messageQueuePrevalence.execute(TestTransaction.class, new Payload(2)).get().getSum());
      Assert.assertEquals(3, messageQueuePrevalence.execute(TestTransaction.class, new Payload(1)).get().getSum());

      Assert.assertEquals(3, prevalence.execute(new Query<Root, Integer>() {
        @Override
        public Integer execute(Root root, OffsetDateTime date) throws Exception {
          return root.getCounter().get();
        }
      }).intValue());


    } finally {
      Assert.assertTrue(service.close());
    }

    // second run, start up again and reload journal

    service = serviceFactory(serviceName);
    try {
      Assert.assertTrue(service.open());

      Prevalence prevalence = service.getInjector().getInstance(Prevalence.class);
      MessageQueuePrevalence messageQueuePrevalence = service.getInjector().getInstance(MessageQueuePrevalence.class);

      Assert.assertEquals(3, prevalence.execute(new Query<Root, Integer>() {
        @Override
        public Integer execute(Root root, OffsetDateTime date) throws Exception {
          return root.getCounter().get();
        }
      }).intValue());

    } finally {
      Assert.assertTrue(service.close());
    }


  }

  private Service serviceFactory(final String serviceName) {
    return new Service(serviceName) {

        @Override
        public List<Class<? extends Initializable>> getInitializables() {
          return Collections.singletonList(MessageQueuePrevalence.class);
        }

        @Override
        public List<Module> getModules() {
          List<Module> modules = new ArrayList<>();
          modules.add(new PrevalenceModule(Root.class, getServiceName()));
          return modules;
        }
      };
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

}
