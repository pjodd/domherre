package se.kodapan.brfduva.service.template.mq;

/**
 * @author kalle
 * @since 2017-02-13 23:01
 */
public interface MessageQueueWriter {

  public abstract  void write(MessageQueueTopic topic, MessageQueueMessage message) throws Exception;

}
