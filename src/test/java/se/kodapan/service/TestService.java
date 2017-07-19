package se.kodapan.service;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import junit.framework.Assert;
import lombok.Data;
import org.junit.Test;
import se.kodapan.service.template.Service;
import se.kodapan.service.template.ServiceModule;
import se.kodapan.service.template.mq.MessageQueueFactory;
import se.kodapan.service.template.mq.ram.RamQueueFactory;
import se.kodapan.service.template.prevalence.PrevalenceModule;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2017-03-07 22:06
 */
public class TestService {

  @Test
  public void test() throws Exception {

    List<Module> modules = new ArrayList<>();
    modules.add(new ServiceModule("test"));
    modules.add(new PrevalenceModule(Root.class) {
      @Override
      public void configure(Binder binder) {
        binder.bind(MessageQueueFactory.class).annotatedWith(Names.named(PrevalenceModule.PREVALENCE_JOURNAL_FACTORY)).to(RamQueueFactory.class);
      }
    });
    Service service = new Service() {
      @Override
      public List<Module> getModules() {
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
