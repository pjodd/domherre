package se.kodapan.brfduva.service.template.kafka;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author kalle
 * @since 2017-02-12 22:05
 */
public class KafkaReader {

  private KafkaTopic topic;
  private KafkaConsumer consumer;

  public KafkaReader(KafkaTopic topic, KafkaConsumer consumer) {
    this.topic = topic;
    this.consumer = consumer;
  }


}
