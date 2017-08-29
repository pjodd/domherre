package se.kodapan.service.template.mq.kafka;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.util.Environment;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * @author kalle
 * @since 2017-08-29
 */
public class KafkaGetQueueEndOffset {

  private Logger log = LoggerFactory.getLogger(getClass());

  private String kafkaBootstrapList = Environment.getValue("kafka.bootstrap.servers", "172.16.61.129:9092");

  private CountDownLatch endLatch = new CountDownLatch(1);
  private long startOffset = -1;
  private long endOffset = -1;

  private MessageQueueTopic topic;

  public KafkaGetQueueEndOffset(MessageQueueTopic topic) {
    this.topic = topic;
  }

  public long execute() throws InterruptedException {

    Properties config = new Properties();

    config.put("bootstrap.servers", kafkaBootstrapList);
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    config.put("auto.offset.reset", "latest");

    config.put("group.id", "kafka-factory-get-end-offset");

    KafkaConsumer kafkaConsumer = new KafkaConsumer<>(config);

    ConsumerRebalanceListener consumerRebalanceListener = new ConsumerRebalanceListener() {

      @Override
      public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        // if latch is down and should just continue
        if(endLatch.getCount() == 0) {
          return;
        }
        // There will only ever be one partition here
        TopicPartition tp = partitions.iterator().next();
        long pos = kafkaConsumer.position(tp);
        if(startOffset == -1) {
          log.debug("Found Kafka start position {} for {}", pos, tp);
          startOffset = pos;
        } else {
          log.debug("Found Kafka start position {}, will use given position {} for ", pos, startOffset, tp);
        }
        kafkaConsumer.seekToEnd(Collections.singletonList(tp));
        endOffset = kafkaConsumer.position(tp);
        log.debug("Found end position {} for {}", endOffset, tp);
        log.debug("Seeking to position {} for {}", startOffset, tp);
        kafkaConsumer.seek(tp, startOffset);
        // Check that this is not an empty topic
        if(endOffset <= 1) {
          endLatch.countDown();
        }
      }

      @Override
      public void onPartitionsRevoked(Collection<TopicPartition> partitions) { }

    };

    kafkaConsumer.subscribe(Collections.singletonList(topic.toString()), consumerRebalanceListener);

    log.trace("Opened Kafka kafkaConsumer to get end offset using config " + config);

    endLatch.await();

    kafkaConsumer.close();

    return endOffset;


  }


}
