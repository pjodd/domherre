package se.kodapan.service.template.mq.localfs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.mq.MessageQueueMessage;
import se.kodapan.service.template.mq.MessageQueueReader;
import se.kodapan.service.template.mq.MessageQueueTopic;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author kalle
 * @since 2017-03-24
 */
public class LocalFsMessageQueueTopic {

  private Logger log = LoggerFactory.getLogger(getClass());

  private LocalFsMessageQueue queue;
  private MessageQueueTopic topic;

  private ObjectMapper objectMapper;

  public LocalFsMessageQueueTopic(LocalFsMessageQueue queue, MessageQueueTopic topic, ObjectMapper objectMapper) {
    this.queue = queue;
    this.topic = topic;
    this.objectMapper = objectMapper;
  }

  private ReadWriteLock lock = new ReentrantReadWriteLock();
  private Set<MessageQueueReader> readers = new HashSet<>();

  void register(LocalFsMessageQueueReader reader) throws Exception {
    if (lock.writeLock().tryLock(1, TimeUnit.MINUTES)) {
      try {
        Iterator<MessageQueueMessage> messages = read();
        while (messages.hasNext()) {
          reader.getConsumer().consume(messages.next());
        }
      } finally {
        lock.writeLock().unlock();
      }
    }
    readers.add(reader);
  }

  void unregister(LocalFsMessageQueueReader reader) {
    readers.remove(reader);
  }

  private File topicPath;
  private File currentJournal;
  private ObjectOutputStream currentJournalOutput;

  public void open() throws IOException {
    topicPath = getTopicPath();
    currentJournal = new File(topicPath, System.currentTimeMillis() + ".journal");
    currentJournalOutput = new ObjectOutputStream(new FileOutputStream(currentJournal));
  }

  public void close() throws IOException {
    currentJournalOutput.close();
  }


  private File getTopicPath() throws IOException {
    File journalPath = new File(queue.getRootPath(), topic.toString());
    if (!journalPath.exists() && !journalPath.mkdirs()) {
      throw new IOException("Unable to mkdirs " + journalPath.getAbsolutePath());
    }
    return journalPath;
  }

  private File[] listJournals() throws IOException {
    File[] journals = topicPath.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isFile() && pathname.getName().matches("\\d+\\.journal");
      }
    });
    Arrays.sort(journals, new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return journals;
  }

  void write(MessageQueueMessage message) throws Exception {
    if (lock.writeLock().tryLock(1, TimeUnit.MINUTES)) {
      try {
        writeMessage(currentJournalOutput, message);
        currentJournalOutput.flush();
        broadcastMessage(message);
      } finally {
        lock.writeLock().unlock();
      }
    }
  }

  private void broadcastMessage(MessageQueueMessage message) {
    for (MessageQueueReader reader : readers) {
      try {
        reader.getConsumer().consume(message);
      } catch (Exception e) {
        log.error("Consumer {} threw an exception while consuming {}", reader, message, e);
      }
    }
  }

  private Iterator<MessageQueueMessage> read() throws IOException {

    Iterator<File> journals = Arrays.asList(listJournals()).iterator();

    Iterator<MessageQueueMessage> iterator = new Iterator<MessageQueueMessage>() {

      ObjectInputStream ois;
      MessageQueueMessage nextMessage;

      @Override
      public boolean hasNext() {
        return nextMessage != null;
      }

      @Override
      public MessageQueueMessage next() {
        MessageQueueMessage currentMessage = this.nextMessage;
        MessageQueueMessage nextMessage;
        try {
          if (ois == null || ois.available() < 1) {
            while (journals.hasNext()) {
              File journal = journals.next();
              ois = new ObjectInputStream(new FileInputStream(journal));
              if (ois.available() > 0) {
                break;
              } else {
                ois.close();
                ois = null;
              }
            }
          }
          if (ois == null) {
            nextMessage = null;
          } else {
            nextMessage = readMessage(ois);
          }
        } catch (IOException ioe) {
          throw new RuntimeException(ioe);
        }
        this.nextMessage = nextMessage;
        return currentMessage;
      }
    };
    iterator.next();
    return iterator;
  }

  private void writeMessage(ObjectOutputStream oos, MessageQueueMessage message) throws IOException {
    oos.writeInt(1); // posting version
    oos.writeLong(message.getIdentity().getMostSignificantBits());
    oos.writeLong(message.getIdentity().getLeastSignificantBits());
    oos.writeUTF(message.getStereotype());
    oos.writeInt(message.getVersion());
    oos.writeUTF(message.getCreated().toString());
    oos.writeUTF(objectMapper.writeValueAsString(message.getPayload()));
  }

  private MessageQueueMessage readMessage(ObjectInputStream ois) throws IOException {
    int postingVersion = ois.readInt();
    if (postingVersion != 1) {
      throw new UnsupportedEncodingException("Expected posting version 1");
    }
    MessageQueueMessage message = new MessageQueueMessage();
    message.setIdentity(new UUID(ois.readLong(), ois.readLong()));
    message.setStereotype(ois.readUTF());
    message.setVersion(ois.readInt());
    message.setCreated(OffsetDateTime.parse(ois.readUTF()));
    message.setPayload(objectMapper.readValue(ois.readUTF(), JsonNode.class));
    return message;
  }


}
