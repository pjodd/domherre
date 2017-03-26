package se.kodapan.service.template.prevalence;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author kalle
 * @since 2017-02-12 22:06
 */
public class Prevalence {

  private Logger log = LoggerFactory.getLogger(getClass());

  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private Object root;

  @Inject
  public Prevalence(@Named("prevalence root") Object root) {
    this.root = root;
  }

  @Getter
  @Setter
  private long defaultLockTimeout = 1;

  @Getter
  @Setter
  private TimeUnit defaultLockTimemoutUnit = TimeUnit.MINUTES;

  public <Response, Root> Response execute(Query<Root, Response> query) throws Exception {
    return execute(query, defaultLockTimeout, defaultLockTimemoutUnit);
  }
  public <Response, Root> Response execute(Query<Root, Response> query, long timeout, TimeUnit timeoutUnit) throws Exception {
    if (lock.readLock().tryLock(timeout, timeoutUnit)) {
      try {
        log.debug("Executing query " + query);
        return query.execute((Root)root, OffsetDateTime.now());
      } finally {
        lock.readLock().unlock();
      }
    } else {
      throw new TimeoutException("Unable to achieve read lock");
    }
  }

  public <Response, Payload, Root> Response execute(Transaction<Root, Payload, Response> transaction, Payload payload, OffsetDateTime created) throws Exception {
    if (lock.writeLock().tryLock(defaultLockTimeout, defaultLockTimemoutUnit)) {
      try {
        log.debug("Executing transaction " + transaction.getClass().getName() + " using payload " + payload);
        return transaction.execute((Root)root, payload, created);
      } finally {
        lock.writeLock().unlock();
      }
    } else {
      throw new TimeoutException("Unable to achieve write lock");
    }
  }

}
