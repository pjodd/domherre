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
import se.kodapan.service.template.util.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Just starts it up and close it down.
 *
 * @author kalle
 * @since 2017-03-07 22:06
 */
public class TestService extends ServiceTest {

  @Test
  public void test() throws Exception {

    int webConfigPort = 9000 + new Random().nextInt(1000);
    Environment.setDefaultValue("webConfig.port", webConfigPort);

    Service service = new Service("test-" + System.currentTimeMillis());

    Assert.assertTrue(service.open());
    try {
      service.run();
      // todo assert ping answers
      service.stop();
    } finally {
      Assert.assertTrue(service.close());
    }
  }
  
}
