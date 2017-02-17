package se.kodapan.brfduva.service.template.mq;

import lombok.Data;
import se.kodapan.brfduva.service.template.Initializable;

/**
 * @author kalle
 * @since 2017-02-13 23:01
 */
public interface MessageQueueReader extends Initializable {

  public abstract void registerConsumer(MessageQueueTopic topic, MessageQueueConsumer consumer);

}
