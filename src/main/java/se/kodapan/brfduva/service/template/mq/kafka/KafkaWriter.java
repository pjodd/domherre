package se.kodapan.brfduva.service.template.mq.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.kafka.clients.consumer.Consumer;
import org.json.JSONObject;
import org.json.JSONTokener;
import se.kodapan.brfduva.service.template.mq.MessageQueueMessage;
import se.kodapan.brfduva.service.template.mq.MessageQueueTopic;
import se.kodapan.brfduva.service.template.mq.MessageQueueWriter;

/**
 * @author kalle
 * @since 2017-02-12 22:05
 */
public class KafkaWriter implements MessageQueueWriter {
  
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
    // todo
  }

}
