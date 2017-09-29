package se.kodapan.service.template.mq.kafka;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.mq.MessageQueueTopic;
import se.kodapan.service.template.util.Environment;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author kalle
 * @since 2017-08-29
 */
public class KafkaGetQueueEndOffset {

  private Logger log = LoggerFactory.getLogger(getClass());

  private String kafkaBootstrapList = Environment.getValue("kafka.bootstrap.servers", "kafka:9092");

  private CountDownLatch endLatch = new CountDownLatch(1);
  private long startOffset = -1;
  private long endOffset = -1;

  private MessageQueueTopic topic;

  public KafkaGetQueueEndOffset(MessageQueueTopic topic) {
    this.topic = topic;
  }

  public long execute() throws InterruptedException, TimeoutException {

    Properties config = new Properties();

    config.put("bootstrap.servers", kafkaBootstrapList);
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    config.put("auto.offset.reset", "earliest");

    config.put("group.id", UUID.randomUUID().toString());

    KafkaConsumer kafkaConsumer = new KafkaConsumer<>(config);

    ConsumerRebalanceListener consumerRebalanceListener = new ConsumerRebalanceListener() {

      @Override
      public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        // if latch is down and should just continue
        if (endLatch.getCount() == 0) {
          return;
        }
        // There will only ever be one partition here
        TopicPartition tp = partitions.iterator().next();
        long pos = kafkaConsumer.position(tp);
        if (startOffset == -1) {
          log.debug("Found Kafka start position {} for {}", pos, tp);
          startOffset = pos;
        } else {
          log.debug("Found Kafka start position {}, will use given position {} for ", pos, startOffset, tp);
        }
        kafkaConsumer.seekToEnd(Collections.singletonList(tp));
        endOffset = kafkaConsumer.position(tp);
        log.debug("Found end position {} for {}", endOffset, tp);
        endLatch.countDown();
      }

      @Override
      public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
      }

    };

    kafkaConsumer.subscribe(Collections.singletonList(topic.toString()), consumerRebalanceListener);

    if (log.isTraceEnabled()) {
      log.trace("Opened Kafka kafkaConsumer to get end offset using config {}", config);
    }

    kafkaConsumer.poll(0); // trigger consumerRebalanceListener

    try {
      // one minute might actually be way too much
      if (!endLatch.await(1, TimeUnit.MINUTES)) {
        throw new TimeoutException("Timeout seeking to end offset of topic " + topic.toString());
      } else {
        log.trace("Found end offset of {} to be {}", topic.toString(), endOffset);
        return endOffset;
      }
    } finally {
      kafkaConsumer.close();
    }


  }


}
