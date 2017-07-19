package se.kodapan.service.template.prevalence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.Initializable;
import se.kodapan.service.template.mq.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author kalle
 * @since 2017-02-12 22:13
 */
@Singleton
public class MessageQueuePrevalence implements Initializable {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  private Prevalence prevalence;

  @Inject
  @Named(PrevalenceModule.PREVALENCE_JOURNAL_FACTORY)
  private MessageQueueFactory journalFactory;

  private MessageQueueReader journalReader;

  private MessageQueueWriter journalWriter;

  @Inject
  @Named(PrevalenceModule.PREVALENCE_JOURNAL_TOPIC)
  private MessageQueueTopic eventSourceTopic;

  @Inject
  private ObjectMapper objectMapper;


  private Map<UUID, AwaitedTransactionExecution> awaitedTransactionExecutions;

  @Override
  public boolean open() throws Exception {

    if (eventSourceTopic == null) {
      log.error("No event source topic set");
      return false;
    }

    awaitedTransactionExecutions = new HashMap<>();

    log.info("Scanning for transaction classes in classpath...");
    eventSourceBindings = new HashSet<>();
    ClassLoader cl = getClass().getClassLoader();
    Set<ClassPath.ClassInfo> classesInPackage = ClassPath.from(cl).getTopLevelClassesRecursive("se.kodapan.service");
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

    journalReader = journalFactory.readerFactory(eventSourceTopic, new MessageQueueConsumer() {
      @Override
      public void consume(MessageQueueMessage message) {

        log.debug("Incoming message " + message);

        AwaitedTransactionExecution awaitedTransactionExecution = awaitedTransactionExecutions.get(message.getIdentity());
        try {

          EventSourceBinding binding = findBinding(message.getStereotype(), message.getVersion());

          if (binding == null) {
            log.error("No binding for event\n" + message);

          } else {

            Object payload;
            Transaction transaction;
            try {
              payload = objectMapper.readValue(objectMapper.writeValueAsString(message.getPayload()), binding.getPayloadClass());
              transaction = binding.getTransactionClass().newInstance();
            } catch (Exception e) {
              log.error("Exception while preparing to execute transaction", e);
              return;
            }
            try {
              Object response = prevalence.execute(transaction, payload, message.getCreated());
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

    if (!journalReader.open()) {
      log.error("Unable to open journal reader");
      return false;
    }

    journalWriter = journalFactory.writerFactory();
    if (!journalWriter.open()) {
      log.error("Unable to open journal writer");
      return false;
    }

    return true;
  }

  @Override
  public boolean close() throws Exception {
    if (!journalReader.close()) {
      return false;
    }
    return true;
  }

  /**
   * Sending transaction to journal and does not wait for execution.
   *
   * @param transactionClass
   * @param payload
   * @param <Response>
   * @param <Payload>
   * @param <Root>
   * @throws Exception
   */
  public <Response, Payload, Root> void send(
      Class<? extends Transaction<Root, Payload, Response>> transactionClass,
      Payload payload
  ) throws Exception {

    log.debug("Executing {} using payload {}", transactionClass.getName(), payload);

    EventSourceBinding eventSourceBinding = findBinding(transactionClass);
    if (eventSourceBinding == null) {
      throw new IllegalStateException("No binding for " + transactionClass.getName());
    }

    MessageQueueMessage message = messageFactory(payload, eventSourceBinding);

    journalWriter.write(eventSourceTopic, message);
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


  /**
   * Executes and waits for response using a future.
   *
   * If you don't care about the response use {@link #send(Class, Object)} instead to save system resources.
   *
   * @param transactionClass
   * @param payload
   * @param <Response>
   * @param <Payload>
   * @param <Root>
   * @return
   * @throws Exception
   */
  public <Response, Payload, Root> Future<Response> execute(
      Class<? extends Transaction<Root, Payload, Response>> transactionClass,
      Payload payload
  ) throws Exception {

    log.debug("Executing {} using payload {}", transactionClass.getName(), payload);

    EventSourceBinding eventSourceBinding = findBinding(transactionClass);
    if (eventSourceBinding == null) {
      throw new IllegalStateException("No binding for " + transactionClass.getName());
    }

    MessageQueueMessage message = messageFactory(payload, eventSourceBinding);

    AwaitedTransactionExecution awaitedTransactionExecution = new AwaitedTransactionExecution();
    awaitedTransactionExecution.setMessageIdentity(message.getIdentity());
    awaitedTransactionExecution.setDoneSignal(new CountDownLatch(1));
    awaitedTransactionExecution.setMessageIdentity(message.getIdentity());
    awaitedTransactionExecution.setCreated(OffsetDateTime.now());
    awaitedTransactionExecutions.put(awaitedTransactionExecution.getMessageIdentity(), awaitedTransactionExecution);

    journalWriter.write(eventSourceTopic, message);

    return new Future<Response>() {
      @Override
      public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean isDone() {
        return awaitedTransactionExecution.getDoneSignal().getCount() == 0;
      }

      @Override
      public Response get() throws InterruptedException, ExecutionException {
        awaitedTransactionExecution.getDoneSignal().await();
        if (awaitedTransactionExecution.getTransactionExecutionException() != null) {
          throw new ExecutionException(awaitedTransactionExecution.getTransactionExecutionException());
        }
        return (Response) awaitedTransactionExecution.getTransactionResponse();
      }

      @Override
      public Response get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!awaitedTransactionExecution.getDoneSignal().await(timeout, unit)) {
          throw new TimeoutException("Timed out while waiting for transaction to be executed. It should still be executed later.");
        }
        if (awaitedTransactionExecution.getTransactionExecutionException() != null) {
          throw new ExecutionException(awaitedTransactionExecution.getTransactionExecutionException());
        }
        return (Response) awaitedTransactionExecution.getTransactionResponse();
      }
    };

  }

  private <Payload> MessageQueueMessage messageFactory(Payload payload, EventSourceBinding eventSourceBinding) throws java.io.IOException {
    MessageQueueMessage message = new MessageQueueMessage();
    message.setIdentity(UUID.randomUUID());
    message.setCreated(OffsetDateTime.now());
    message.setPayload(objectMapper.readValue(objectMapper.writeValueAsString(payload), JsonNode.class));
    message.setStereotype(eventSourceBinding.getStereotype());
    message.setVersion(eventSourceBinding.getVersion());
    return message;
  }


  /**
   * @param stereotype
   * @param version
   * @param payloadClass
   * @param transactionClass
   * @param <PayloadClass>
   * @throws IllegalStateException If stereotype and version is already bound.
   */
  public synchronized <PayloadClass, Root> void registerEventSourceBinding(
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
    private Class<? extends Transaction<? extends Object, ? extends Object, ? extends Object>> transactionClass;

    public EventSourceBinding(
        String stereotype,
        int version,
        Class payloadClass,
        Class<? extends Transaction<? extends Object, ? extends Object, ? extends Object>> transactionClass
    ) {
      this.stereotype = stereotype;
      this.version = version;
      this.payloadClass = payloadClass;
      this.transactionClass = transactionClass;
    }
  }

}
