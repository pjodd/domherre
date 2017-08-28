package se.kodapan.service.template.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kalle
 * @since 2017-08-29 00:37
 */
public class MessageQueueMessageBuilder {

  private static Logger log = LoggerFactory.getLogger(MessageQueueMessageBuilder.class);

  private MessageQueueMessage message;

  private ObjectMapper objectMapper;

  public MessageQueueMessageBuilder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    message = new MessageQueueMessage();
  }

  public MessageQueueMessageBuilder setStereotype(String stereotype) {
    message.setStereotype(stereotype);
    return this;
  }

  public MessageQueueMessageBuilder setVersion(int version) {
    message.setVersion(version);
    return this;
  }

  public MessageQueueMessageBuilder setPayload(Object payload) {
    if (payload != null) {
      try {
        message.setPayload(objectMapper.readValue(objectMapper.writeValueAsString(payload), JsonNode.class));
      } catch (Exception e) {
        log.error("Unable to serialize payload {} to JsonNode", payload, e);
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  public MessageQueueMessage build() {
    return message;
  }

}
