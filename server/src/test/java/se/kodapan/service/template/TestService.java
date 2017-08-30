package se.kodapan.service.template;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import junit.framework.Assert;
import lombok.Data;
import org.junit.Test;
import se.kodapan.service.template.mq.MessageQueueFactory;
import se.kodapan.service.template.mq.kafka.KafkaFactory;
import se.kodapan.service.template.prevalence.MessageQueuePrevalence;
import se.kodapan.service.template.prevalence.PrevalenceModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Just starts it up and close it down.
 *
 * @author kalle
 * @since 2017-03-07 22:06
 */
public class TestService extends ServiceTest {

  @Test
  public void test() throws Exception {


    Service service = new Service("test-" + System.currentTimeMillis()) {

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

    Assert.assertTrue(service.open());
    try {


      System.currentTimeMillis();

      service.run();
//      while (true) {
//        Thread.sleep(1000);
//      }

    } finally {
      Assert.assertTrue(service.close());
    }
  }

  @Data
  public static class Root {

  }

}
