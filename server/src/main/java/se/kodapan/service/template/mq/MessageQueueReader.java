package se.kodapan.service.template.mq;

import se.kodapan.service.template.Initializable;

/**
 * @author kalle
 * @since 2017-02-13 23:01
 */
public interface MessageQueueReader extends Initializable {

  public abstract MessageQueueReaderConfiguration getConfiguration();

  public abstract MessageQueueConsumer getConsumer();

  public abstract boolean seek(long offset);

}
