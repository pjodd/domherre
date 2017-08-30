package se.kodapan.service.template.mq;

import lombok.Data;

/**
 * @author kalle
 * @since 2017-08-29 19:57
 */
@Data
public class MessageQueueConsumerContext {

  private MessageQueueTopic topic;
  private long offset;

}
