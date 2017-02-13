package se.kodapan.brfduva.service.template.kafka;

import lombok.Data;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author kalle
 * @since 2017-02-12 22:16
 */
@Data
public class KafkaMessage {

  public UUID identity;
  public OffsetDateTime created;

  public String stereotype;
  public int version;

  public String payload;


  public static KafkaMessage fromJSON(String raw) {
    JSONObject json = new JSONObject(new JSONTokener(raw));
    KafkaMessage message = new KafkaMessage();
    message.setIdentity(UUID.fromString(json.getString("identity")));
    message.setCreated(OffsetDateTime.parse(json.getString("created")));
    message.setStereotype(json.getString("stereotype"));
    message.setVersion(json.getInt("version"));
    if (json.has("payload")) {
      message.setPayload(json.getJSONObject("payload").toString());
    }
    return message;
  }

}
