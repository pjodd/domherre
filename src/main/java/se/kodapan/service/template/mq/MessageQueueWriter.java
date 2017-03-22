package se.kodapan.service.template.mq;

import se.kodapan.service.template.Initializable;

/**
 * @author kalle
 * @since 2017-02-13 23:01
 */
public interface MessageQueueWriter extends Initializable {

  public abstract  void write(MessageQueueTopic topic, MessageQueueMessage message) throws Exception;

}
