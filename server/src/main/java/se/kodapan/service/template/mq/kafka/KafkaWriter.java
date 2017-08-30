package se.kodapan.service.template.mq.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.mq.MessageQueueMessage;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.mq.MessageQueueWriter;
import se.kodapan.service.template.util.Environment;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * @author kalle
 * @since 2017-02-12 22:05
 */
public class KafkaWriter implements MessageQueueWriter {

  private Logger log = LoggerFactory.getLogger(getClass());

  private ObjectMapper objectMapper;

  private KafkaProducer<String, String> kafkaProducer;

  private String kafkaBootstrapList = Environment.getValue("kafka.bootstrap.servers", "localhost:9092");

  private Map<String, String> getAdditionalKafkaProperties() {
    return Collections.EMPTY_MAP;
  }

  public KafkaWriter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean open() throws Exception {

    Properties config = new Properties();

    config.put("bootstrap.servers", getKafkaBootstrapList());
    config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    
    config.putAll(getAdditionalKafkaProperties());

    kafkaProducer = new KafkaProducer<>(config);

    return true;
  }

  @Override
  public boolean close() throws Exception {
    if (kafkaProducer != null) {
      kafkaProducer.close();
      kafkaProducer = null;
    }
    return true;
  }


  @Override
  public void write(MessageQueueTopic topic, MessageQueueMessage message) throws Exception {
    write(topic, objectMapper.writeValueAsString(message));
  }

  public void write(MessageQueueTopic topic, String message) throws Exception {
    Future<RecordMetadata> future = kafkaProducer.send(new ProducerRecord<>(topic.toString(), message));
    // todo really wait for the future?
    RecordMetadata recordMetadata = future.get();
    System.currentTimeMillis();
  }

  public String getKafkaBootstrapList() {
    return kafkaBootstrapList;
  }

  public void setKafkaBootstrapList(String kafkaBootstrapList) {
    this.kafkaBootstrapList = kafkaBootstrapList;
  }


}
