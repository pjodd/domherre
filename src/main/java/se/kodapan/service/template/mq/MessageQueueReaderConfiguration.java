package se.kodapan.service.template.mq;

import lombok.Data;

import java.util.UUID;

/**
 * @author kalle
 * @since 2017-08-22 17:50
 */
@Data
public class MessageQueueReaderConfiguration {

  public enum StartOffset {
    earliest,
    latest
  }

  private StartOffset startOffset = StartOffset.earliest;
  
  private String group = UUID.randomUUID().toString();

  private MessageQueueTopic topic;


  public MessageQueueReaderConfiguration(MessageQueueTopic topic) {
    this.topic = topic;
  }

  public MessageQueueReaderConfiguration(MessageQueueTopic topic, StartOffset startOffset, String group) {
    this.topic = topic;
    this.startOffset = startOffset;
    this.group = group;
  }
}
