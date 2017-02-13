package se.kodapan.brfduva.service.template.kafka;

/**
 * @author kalle
 * @since 2017-02-12 22:15
 */
public interface KafkaConsumer {

  public abstract void consume(KafkaMessage message);

}
