package se.kodapan.brfduva.service.template.prevalence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.brfduva.service.template.Initializable;
import se.kodapan.brfduva.service.template.mq.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author kalle
 * @since 2017-02-12 22:13
 */
public class MessageQueuePrevalence<Root> implements Initializable {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  private Prevalence prevalence;

  @Inject
  private MessageQueueReader journalReader;

  @Inject
  private MessageQueueWriter journalWriter;

  @Inject
  @Named("prevalence journal topic")
  private MessageQueueTopic eventSourceTopic;

  @Inject
  private ObjectMapper objectMapper;


  private Map<UUID, AwaitedTransactionExecution> awaitedTransactionExecutions;

  @Override
  public boolean open() throws Exception {

    if (!journalReader.open()) {
      return false;
    }

    awaitedTransactionExecutions = new HashMap<>();

    log.info("Scanning for transaction classes in classpath...");
    eventSourceBindings = new HashSet<>();
    ClassLoader cl = getClass().getClassLoader();
    Set<ClassPath.ClassInfo> classesInPackage = ClassPath.from(cl).getAllClasses();
    for (ClassPath.ClassInfo classInfo : classesInPackage) {
      Class transactionClass;
      try {
        transactionClass = classInfo.load();
      } catch (NoClassDefFoundError e) {
//        log.debug("Exception while inspecting class " + classInfo.getName(), e);
        continue;
      }
      if (!transactionClass.equals(Transaction.class) && Transaction.class.isAssignableFrom(transactionClass)) {
        Transaction transaction = (Transaction) transactionClass.newInstance();
        registerEventSourceBinding(
            transaction.getStereotype(),
            transaction.getVersion(),
            transaction.getPayloadClass(),
            transactionClass
        );
      }
    }
    log.info("Bound " + eventSourceBindings.size() + " transaction classes in classpath.");

    journalReader.registerConsumer(eventSourceTopic, new MessageQueueConsumer() {
      @Override
      public void consume(MessageQueueMessage message) {

        AwaitedTransactionExecution awaitedTransactionExecution = awaitedTransactionExecutions.get(message.getIdentity());
        try {

          EventSourceBinding binding = findBinding(message.getStereotype(), message.getVersion());

          if (binding == null) {
            log.error("No binding for event\n" + message);

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
              if (awaitedTransactionExecution != null) {
                awaitedTransactionExecution.setTransactionResponse(response);
                awaitedTransactionExecution.setExecuted(OffsetDateTime.now());
              }
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

  @Override
  public boolean close() throws Exception {
    if (!journalReader.close()) {
      return false;
    }
    return true;
  }


  @Data
  private static class AwaitedTransactionExecution {
    private UUID messageIdentity;
    private OffsetDateTime created;
    private OffsetDateTime executed;
    private Exception transactionExecutionException;
    private Object transactionResponse;
    private CountDownLatch doneSignal;
  }

  private TimeUnit defaultExecuteTimeoutUnit = TimeUnit.MINUTES;
  private long defaultExecuteTimeoutAmount = 1;

  public <Response, Payload> Response execute(
      Class<? extends Transaction<Root, Payload, Response>> transactionClass,
      Payload payload) throws Exception {
    return execute(transactionClass, payload, defaultExecuteTimeoutUnit, defaultExecuteTimeoutAmount);
  }

  public <Response, Payload> Response execute(
      Class<? extends Transaction<Root, Payload, Response>> transactionClass,
      Payload payload,
      TimeUnit timeoutUnit,
      long timeoutAmount
  ) throws Exception {

    EventSourceBinding eventSourceBinding = findBinding(transactionClass);
    if (eventSourceBinding == null) {
      throw new IllegalStateException("No binding for " + transactionClass.getName());
    }

    MessageQueueMessage message = new MessageQueueMessage();
    message.setIdentity(UUID.randomUUID());
    message.setCreated(OffsetDateTime.now());
    message.setPayload(objectMapper.writeValueAsString(payload));
    message.setStereotype(eventSourceBinding.getStereotype());
    message.setVersion(eventSourceBinding.getVersion());

    AwaitedTransactionExecution awaitedTransactionExecution = new AwaitedTransactionExecution();
    awaitedTransactionExecution.setMessageIdentity(message.getIdentity());
    awaitedTransactionExecution.setDoneSignal(new CountDownLatch(1));
    awaitedTransactionExecution.setMessageIdentity(message.getIdentity());
    awaitedTransactionExecution.setCreated(OffsetDateTime.now());
    awaitedTransactionExecutions.put(awaitedTransactionExecution.getMessageIdentity(), awaitedTransactionExecution);

    journalWriter.write(eventSourceTopic, message);

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

    log.info("Registered event source binding " + binding);
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
