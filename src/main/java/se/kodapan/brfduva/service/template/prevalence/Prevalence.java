package se.kodapan.brfduva.service.template.prevalence;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.name.Named;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author kalle
 * @since 2017-02-12 22:06
 */
public class Prevalence<Root> {

  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private Root root;

  @Inject
  public Prevalence(@Named("root") Object root) {
    this.root = (Root)root;
  }

  @Getter
  @Setter
  private long defaultLockTimeout = 1;

  @Getter
  @Setter
  private TimeUnit defaultLockTimemoutUnit = TimeUnit.MINUTES;

  public <Response> Response execute(Query<Root, Response> query) throws Exception {
    return execute(query, defaultLockTimeout, defaultLockTimemoutUnit);
  }
  public <Response> Response execute(Query<Root, Response> query, long timeout, TimeUnit timeoutUnit) throws Exception {
    if (lock.readLock().tryLock(timeout, timeoutUnit)) {
      try {
        return query.execute(root, OffsetDateTime.now());
      } finally {
        lock.readLock().unlock();
      }
    } else {
      throw new TimeoutException("Unable to achieve read lock");
    }
  }

  public <Response, Payload> Response execute(Transaction<Root, Payload, Response> transaction, Payload payload) throws Exception {
    if (lock.writeLock().tryLock(defaultLockTimeout, defaultLockTimemoutUnit)) {
      try {
        return transaction.execute(root, payload);
      } finally {
        lock.writeLock().unlock();
      }
    } else {
      throw new TimeoutException("Unable to achieve write lock");
    }
  }

}
