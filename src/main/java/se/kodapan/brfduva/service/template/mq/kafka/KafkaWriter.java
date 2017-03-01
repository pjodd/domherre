package se.kodapan.brfduva.service.template.mq.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.brfduva.service.template.mq.MessageQueueMessage;
import se.kodapan.brfduva.service.template.mq.MessageQueueTopic;
import se.kodapan.brfduva.service.template.mq.MessageQueueWriter;
import se.kodapan.brfduva.service.template.util.Environment;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * @author kalle
 * @since 2017-02-12 22:05
 */
public class KafkaWriter implements MessageQueueWriter {

  private Logger log = LoggerFactory.getLogger(getClass());

  private KafkaProducer<String, String> kafkaProducer;

  private String kafkaBootstrapList = Environment.getValue("kafka.bootstrap.servers", "172.17.0.3:9092");

  private Map<String, String> getAdditionalKafkaProperties() {
    return Collections.EMPTY_MAP;
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
    JSONObject json = new JSONObject();
    json.put("stereotype", message.getStereotype());
    json.put("version", message.getVersion());
    json.put("identity", message.getIdentity().toString());
    json.put("created", message.getCreated().toString());
    json.put("payload", new JSONObject(new JSONTokener(message.getPayload())));
    write(topic, json.toString());
  }

  public void write(MessageQueueTopic topic, String message) throws Exception {
    kafkaProducer.send(new ProducerRecord<>(topic.toString(), message));
  }

  public String getKafkaBootstrapList() {
    return kafkaBootstrapList;
  }

  public void setKafkaBootstrapList(String kafkaBootstrapList) {
    this.kafkaBootstrapList = kafkaBootstrapList;
  }


}
