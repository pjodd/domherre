package se.kodapan.brfduva.service.template.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author kalle
 * @since 2017-02-12 22:05
 */
public class KafkaWriter {

  private ObjectMapper objectMapper;

  public void write(KafkaTopic topic, KafkaMessage message) throws Exception {
    JSONObject json = new JSONObject();
    json.put("stereotype", message.getStereotype());
    json.put("version", message.getVersion());
    json.put("identity", message.getIdentity().toString());
    json.put("created", message.getCreated().toString());
    json.put("payload", new JSONObject(new JSONTokener(message.getPayload())));
    write(topic, json.toString());
  }

  public void write(KafkaTopic topic, String message) throws Exception {
    // todo
  }

}
