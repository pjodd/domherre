package se.kodapan.service.template.mq.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.mq.*;
import se.kodapan.service.template.util.Environment;

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

  private String kafkaBootstrapList = Environment.getValue("kafka.bootstrap.servers", "172.16.61.129:9092");

  private Map<String, String> getAdditionalKafkaProperties() {
    return Collections.EMPTY_MAP;
  }

  public KafkaReader(MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer, ObjectMapper objectMapper) {
    super(configuration, consumer, objectMapper);
  }

  @Override
  public boolean open() throws Exception {

    Properties config = new Properties();

    config.put("bootstrap.servers", kafkaBootstrapList);
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("auto.offset.reset", getConfiguration().getStartOffset().name());
    config.put("group.id", getConfiguration().getGroup());

    config.putAll(getAdditionalKafkaProperties());

    kafkaConsumer = new KafkaConsumer<>(config);
    kafkaConsumer.subscribe(Collections.singletonList(getConfiguration().getTopic().toString()));

    log.trace("Opened Kafka kafkaConsumer using config " + config);

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
                  MessageQueueMessage message = getObjectMapper().readValue(record.value(), MessageQueueMessage.class);
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

}
