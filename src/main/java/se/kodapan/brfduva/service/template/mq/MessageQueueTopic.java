package se.kodapan.brfduva.service.template.mq;

import lombok.Data;

/**
 * @author kalle
 * @since 2017-02-12 22:05
 */
@Data
public class MessageQueueTopic {

  private String name;

  /** event-source, command, etc */
  private String stereotype;

  public MessageQueueTopic(String name, String stereotype) {
    // todo assert not too long
    // todo assert lower case
    // todo assert stereotype only alpha
    this.name = name;
    this.stereotype = stereotype;
  }

  @Override
  public String toString() {
    return name + "-" + stereotype;
  }

}
