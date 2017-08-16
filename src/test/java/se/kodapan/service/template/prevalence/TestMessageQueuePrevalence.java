package se.kodapan.service.template.prevalence;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import junit.framework.Assert;
import lombok.Data;
import org.junit.Test;
import se.kodapan.service.template.ServiceModule;
import se.kodapan.service.template.mq.MessageQueueFactory;
import se.kodapan.service.template.mq.ram.RamQueueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kalle
 * @since 2017-02-17 15:45
 */
public class TestMessageQueuePrevalence {

  @Test
  public void test() throws Exception {

    List<Module> modules = new ArrayList<>();
    modules.add(new ServiceModule("test"));
    modules.add(new PrevalenceModule(Root.class, "test") {
      @Override
      public void configure(Binder binder) {
        binder.bind(MessageQueueFactory.class).annotatedWith(Names.named(PrevalenceModule.PREVALENCE_JOURNAL_FACTORY)).to(RamQueueFactory.class);
      }
    });
    Injector injector = Guice.createInjector(modules);

    Prevalence prevalence = injector.getInstance(Prevalence.class);
    MessageQueuePrevalence messageQueuePrevalence = injector.getInstance(MessageQueuePrevalence.class);
    Assert.assertTrue(messageQueuePrevalence.open());

    Assert.assertEquals(2, messageQueuePrevalence.execute(TestTransaction.class, new Payload(2)).get().getSum());
    Assert.assertEquals(3, messageQueuePrevalence.execute(TestTransaction.class, new Payload(1)).get().getSum());

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

}
