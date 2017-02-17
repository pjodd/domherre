package se.kodapan.brfduva.service.template.mq;

import lombok.Data;
import se.kodapan.brfduva.service.template.Initializable;

/**
 * @author kalle
 * @since 2017-02-13 23:01
 */
@Data
public abstract class MessageQueueReader implements Initializable {

  private MessageQueueConsumer consumer;

  public abstract void subscribe(MessageQueueTopic topic);


}
