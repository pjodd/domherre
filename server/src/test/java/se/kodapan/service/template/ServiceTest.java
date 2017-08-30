package se.kodapan.service.template;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import se.kodapan.service.template.util.Environment;

import java.io.IOException;
import java.util.Properties;

/**
 * @author kalle
 * @since 2017-08-29 20:59
 */
public abstract class ServiceTest {

  private KafkaTestFixture server;

  @Before
  public void setup() throws Exception {

    Environment.setDefaultValue("kafka.bootstrap.servers", "localhost:9092");

    server = new KafkaTestFixture();
    server.start(serverProperties());
  }

  @After
  public void teardown() throws Exception {
    server.stop();
  }

  private Properties serverProperties() {
    Properties props = new Properties();
    props.put("zookeeper.connect", "localhost:2181");
    props.put("broker.id", "1");
    return props;
  }

  private static class KafkaTestFixture {
    private TestingServer zk;
    private KafkaServerStartable kafka;

    public void start(Properties properties) throws Exception {
      Integer port = getZkPort(properties);
      zk = new TestingServer(port);
      zk.start();

      KafkaConfig kafkaConfig = new KafkaConfig(properties);
      kafka = new KafkaServerStartable(kafkaConfig);
      kafka.startup();
    }

    public void stop() throws IOException {
      kafka.shutdown();
      zk.stop();
      zk.close();
    }

    private int getZkPort(Properties properties) {
      String url = (String) properties.get("zookeeper.connect");
      String port = url.split(":")[1];
      return Integer.valueOf(port);
    }
  }

}
