package se.kodapan.brfduva.service.template.prevalence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.brfduva.service.template.kafka.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author kalle
 * @since 2017-02-12 22:13
 */
public class KafkaPrevalenceEventBus<Root> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Prevalence<Root> prevalence;

  private KafkaReader kafkaReader;

  private KafkaWriter kafkaWriter;
  private KafkaTopic eventSourceTopic;

  private ObjectMapper objectMapper;

  private Map<UUID, AwaitedTransactionExecution> awaitedTransactionExecutions;

  public boolean init() {

    awaitedTransactionExecutions = new HashMap<>();

    eventSourceBindings = new HashSet<>();
    // todo bindings needs to be registered prior to init!


    kafkaReader = new KafkaReader(new KafkaConsumer() {
      @Override
      public void consume(KafkaMessage message) {

        AwaitedTransactionExecution awaitedTransactionExecution = awaitedTransactionExecutions.get(message.getIdentity());
        try {
          
          EventSourceBinding binding = findBinding(message.getStereotype(), message.getVersion());

          if (binding == null) {
            log.error("No binding for event\n" + message);
            return;
          } else {

            Object payload;
            Transaction transaction;
            try {
              payload = objectMapper.readValue(message.getPayload(), binding.getPayloadClass());
              transaction = binding.getTransactionClass().newInstance();
            } catch (Exception e) {
              log.error("Exception while preparing to execute transaction", e);
              return;
            }
            try {
              Object response = prevalence.execute(transaction, payload);
              awaitedTransactionExecution.setTransactionResponse(response);
              awaitedTransactionExecution.setExecuted(OffsetDateTime.now());
            } catch (Exception e) {
              log.error("Exception while executing transaction", e);
              if (awaitedTransactionExecution != null) {
                awaitedTransactionExecution.setTransactionExecutionException(e);
              }
            }

          }
        } finally {
          if (awaitedTransactionExecution != null) {
            awaitedTransactionExecution.getDoneSignal().countDown();
            awaitedTransactionExecutions.remove(message.getIdentity());
          }
        }
      }
    });

    return true;
  }

  @Data
  private static class AwaitedTransactionExecution {
    private UUID eventIdentity;
    private OffsetDateTime created;
    private OffsetDateTime executed;
    private Exception transactionExecutionException;
    private Object transactionResponse;
    private CountDownLatch doneSignal;
  }

  private TimeUnit defaultExecuteTimeoutUnit = TimeUnit.MINUTES;
  private long defaultExecuteTimeoutAmount = 1;

  public <Response, Payload> Response execute(
      Class<Transaction<Root, Payload, Response>> transactionClass,
      Payload payload) throws Exception {
    return execute(transactionClass, payload, defaultExecuteTimeoutUnit, defaultExecuteTimeoutAmount);
  }

  public <Response, Payload> Response execute(
      Class<Transaction<Root, Payload, Response>> transactionClass,
      Payload payload,
      TimeUnit timeoutUnit,
      long timeoutAmount
  ) throws Exception {

    EventSourceBinding eventSourceBinding = findBinding(transactionClass);
    if (eventSourceBinding == null) {
      throw new IllegalStateException("No binding for " + transactionClass.getName());
    }

    KafkaMessage kafkaMessage = new KafkaMessage();
    kafkaMessage.setIdentity(UUID.randomUUID());
    kafkaMessage.setCreated(OffsetDateTime.now());
    kafkaMessage.setPayload(objectMapper.writeValueAsString(payload));
    kafkaMessage.setStereotype(eventSourceBinding.getStereotype());
    kafkaMessage.setVersion(eventSourceBinding.getVersion());

    AwaitedTransactionExecution awaitedTransactionExecution = new AwaitedTransactionExecution();
    awaitedTransactionExecution.setDoneSignal(new CountDownLatch(1));
    awaitedTransactionExecution.setEventIdentity(kafkaMessage.getIdentity());
    awaitedTransactionExecution.setCreated(OffsetDateTime.now());
    awaitedTransactionExecutions.put(awaitedTransactionExecution.getEventIdentity(), awaitedTransactionExecution);

    kafkaWriter.write(eventSourceTopic, kafkaMessage);

    if (!awaitedTransactionExecution.getDoneSignal().await(timeoutAmount, timeoutUnit)) {
      // todo pass down doneSignal?
      throw new TimeoutException("Timed out while waiting for transaction to be executed. It might still be executed later");
    }
    if (awaitedTransactionExecution.getTransactionExecutionException() != null) {
      throw awaitedTransactionExecution.getTransactionExecutionException();
    }
    return (Response) awaitedTransactionExecution.getTransactionResponse();
  }


  /**
   * @param stereotype
   * @param version
   * @param payloadClass
   * @param transactionClass
   * @param <PayloadClass>
   * @throws IllegalStateException If stereotype and version is already bound.
   */
  public synchronized <PayloadClass> void registerEventSourceBinding(
      String stereotype,
      int version,
      Class<PayloadClass> payloadClass,
      Class<? extends Transaction<Root, PayloadClass, ? extends Object>> transactionClass
  ) throws IllegalStateException {


    EventSourceBinding binding = findBinding(stereotype, version);
    if (binding != null) {
      throw new IllegalStateException("Already bound to " + binding);
    }
    binding = findBinding(transactionClass);
    if (binding != null) {
      throw new IllegalStateException("Already bound to " + binding);
    }
    binding = new EventSourceBinding(stereotype, version, payloadClass, transactionClass);
    eventSourceBindings.add(binding);
  }

  private Set<EventSourceBinding> eventSourceBindings;

  private EventSourceBinding findBinding(String stereotype, int version) {
    for (EventSourceBinding eventSourceBinding : eventSourceBindings) {
      if (stereotype.equals(eventSourceBinding.getStereotype())
          && version == eventSourceBinding.getVersion()) {
        return eventSourceBinding;
      }
    }
    return null;
  }

  private EventSourceBinding findBinding(Class<? extends Transaction> transactionClass) {
    for (EventSourceBinding eventSourceBinding : eventSourceBindings) {
      if (transactionClass.equals(eventSourceBinding.getTransactionClass())) {
        return eventSourceBinding;
      }
    }
    return null;
  }

  @Data
  private class EventSourceBinding {
    private String stereotype;
    private int version;

    private Class payloadClass;
    private Class<? extends Transaction<Root, ? extends Object, ? extends Object>> transactionClass;

    public EventSourceBinding(
        String stereotype,
        int version,
        Class payloadClass,
        Class<? extends Transaction<Root, ? extends Object, ? extends Object>> transactionClass
    ) {
      this.stereotype = stereotype;
      this.version = version;
      this.payloadClass = payloadClass;
      this.transactionClass = transactionClass;
    }
  }

}
