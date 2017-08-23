package se.kodapan.service.template.mq;

import lombok.Data;

import java.util.UUID;

/**
 * @author kalle
 * @since 2017-08-22 17:50
 */
@Data
public class MessageQueueReaderConfiguration {

  public enum AutoOffsetReset {
    earliest,
    latest
  }

  /**
   * Where to start accessing first time reader with the given groups is created.
   * I.e. at start or at end of queue
   */
  private AutoOffsetReset autoOffsetReset = AutoOffsetReset.earliest;
  
  private String group = UUID.randomUUID().toString();

  private MessageQueueTopic topic;

  public MessageQueueReaderConfiguration(MessageQueueTopic topic) {
    this.topic = topic;
  }

  public MessageQueueReaderConfiguration(MessageQueueTopic topic, AutoOffsetReset autoOffsetReset, String group) {
    this.topic = topic;
    this.autoOffsetReset = autoOffsetReset;
    this.group = group;
  }
}
