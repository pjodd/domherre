package se.kodapan.service.template.mq;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author kalle
 * @since 2017-02-13 23:02
 */
@Data
@EqualsAndHashCode
public class MessageQueueMessage {

  public UUID identity = UUID.randomUUID();
  public OffsetDateTime created = OffsetDateTime.now();

  public String stereotype;
  public int version;

  public JsonNode payload;
}
