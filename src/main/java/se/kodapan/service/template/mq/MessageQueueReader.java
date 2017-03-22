package se.kodapan.service.template.mq;

import se.kodapan.service.template.Initializable;

/**
 * @author kalle
 * @since 2017-02-13 23:01
 */
public interface MessageQueueReader extends Initializable {

  public abstract MessageQueueTopic getTopic();

  public abstract MessageQueueConsumer getConsumer();


}