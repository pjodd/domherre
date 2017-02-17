package se.kodapan.brfduva.service.template.mq.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.brfduva.service.template.mq.AbstractMessageQueueReader;
import se.kodapan.brfduva.service.template.mq.MessageQueueConsumer;
import se.kodapan.brfduva.service.template.mq.MessageQueueMessage;
import se.kodapan.brfduva.service.template.mq.MessageQueueTopic;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author kalle
 * @since 2017-02-12 22:05
 */
public class KafkaReader extends AbstractMessageQueueReader {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Poller poller;
  private Consumer<String, String> kafkaConsumer;

  private String kafkaBootstrapList = ""; // todo IP from environment variable

  private Map<String, String> getAdditionalKafkaProperties() {
    return Collections.EMPTY_MAP;
  }

  @Override
  public void registerConsumer(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    super.registerConsumer(topic, consumer);
    kafkaConsumer.subscribe(Collections.singletonList(topic.toString()));
  }

  @Override
  public boolean open() throws Exception {

    Properties config = new Properties();

    config.put("bootstrap.servers", getKafkaBootstrapList());
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("group.id", UUID.randomUUID().toString());

    config.putAll(getAdditionalKafkaProperties());

    log.trace("Opened Kafka kafkaConsumer using config " + config);

    kafkaConsumer = new KafkaConsumer<>(config);

    poller = new Poller();
    Thread pollerThread = new Thread(poller);
    pollerThread.setDaemon(true);
    pollerThread.setName("Kafka poller thread");
    pollerThread.start();

    return true;
  }

  @Override
  public boolean close() throws Exception {

    boolean success = true;

    if (poller != null) {
      poller.stopSignal.set(true);
      if (!poller.doneSignal.await(1, TimeUnit.MINUTES)) {
        log.error("Timed out waiting for poller to stop");
        success = false;
      } else {
        poller = null;
      }
    }

    if (kafkaConsumer != null) {
      kafkaConsumer.close();
      kafkaConsumer = null;
    }

    return success;
  }

  private class Poller implements Runnable {

    private AtomicBoolean stopSignal;
    private CountDownLatch doneSignal;

    @Override
    public void run() {
      stopSignal = new AtomicBoolean(false);
      doneSignal = new CountDownLatch(1);
      try {
        while (!stopSignal.get()) {
          try {
            if (getConsumer() == null) {
              Thread.sleep(TimeUnit.SECONDS.toMillis(1));
              continue;
            }
            ConsumerRecords<String, String> records = kafkaConsumer.poll(TimeUnit.SECONDS.toMillis(1));
            try {
              if (!records.isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                  MessageQueueMessage message = MessageQueueMessage.fromJSON(record.value());
                  try {
                    getConsumer().consume(message);
                  } catch (Exception e) {
                    log.error("Exception while consuming message\n" + message, e);
                  }
                }
              }
            } finally {
              kafkaConsumer.commitAsync();
            }
          } catch (Exception e) {
            log.error("Exception in Kafka poller thread", e);
          }
        }
      } finally {
        doneSignal.countDown();
      }
    }

  }

  public String getKafkaBootstrapList() {
    return kafkaBootstrapList;
  }

  public void setKafkaBootstrapList(String kafkaBootstrapList) {
    this.kafkaBootstrapList = kafkaBootstrapList;
  }
}