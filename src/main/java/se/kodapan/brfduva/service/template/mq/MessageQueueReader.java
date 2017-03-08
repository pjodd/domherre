package se.kodapan.brfduva.service.template.mq;

import lombok.Data;
import lombok.Getter;
import se.kodapan.brfduva.service.template.Initializable;

/**
 * @author kalle
 * @since 2017-02-13 23:01
 */
public interface MessageQueueReader extends Initializable {

  public abstract MessageQueueTopic getTopic();

  public abstract MessageQueueConsumer getConsumer();


}
