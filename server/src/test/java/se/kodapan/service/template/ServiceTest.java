package se.kodapan.service.template;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.util.Environment;

import java.io.File;
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

    FileUtils.deleteDirectory(new File("/tmp/kafka-logs"));

    Environment.setDefaultValue("kafka.bootstrap.servers", "localhost:9092");

    server = new KafkaTestFixture();
    server.start(serverProperties());
  }

  public void createTopic(MessageQueueTopic topic) {
    ZkClient zkClient = null;
    ZkUtils zkUtils = null;
    try {
      String zookeeperHosts = "localhost:2181"; // If multiple zookeeper then -> String zookeeperHosts = "192.168.20.1:2181,192.168.20.2:2181";
      int sessionTimeOutInMs = 15 * 1000; // 15 secs
      int connectionTimeOutInMs = 10 * 1000; // 10 secs

      zkClient = new ZkClient(zookeeperHosts, sessionTimeOutInMs, connectionTimeOutInMs, ZKStringSerializer$.MODULE$);
      zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperHosts), false);

      String topicName = topic.toString();
      int noOfPartitions = 1;
      int noOfReplication = 1;
      Properties topicConfiguration = new Properties();

      AdminUtils.createTopic(zkUtils, topicName, noOfPartitions, noOfReplication, topicConfiguration, new RackAwareMode.Safe$());

    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (zkClient != null) {
        zkClient.close();
      }
    }
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
