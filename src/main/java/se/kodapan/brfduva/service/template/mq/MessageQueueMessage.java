package se.kodapan.brfduva.service.template.mq;

import lombok.Data;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author kalle
 * @since 2017-02-13 23:02
 */
@Data
public class MessageQueueMessage {

  public UUID identity;
  public OffsetDateTime created;

  public String stereotype;
  public int version;

  public String payload;

  public static MessageQueueMessage fromJSON(String raw) {
    JSONObject json = new JSONObject(new JSONTokener(raw));
    MessageQueueMessage message = new MessageQueueMessage();
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
