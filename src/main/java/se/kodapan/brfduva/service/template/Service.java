package se.kodapan.brfduva.service.template;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kalle
 * @since 2017-02-12 22:14
 */
@Singleton
public class Service implements Initializable {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Inject
  private ServiceModule serviceModule;

  @Inject
  private Binder binder;

  @Override
  public boolean open() throws Exception {
    serviceModule.configure(binder);

    // todo retry for a while if unable
    for (Initializable initializable : serviceModule.getInitializables()) {
      if (!initializable.open()) {
        log.error("Unable to open " + initializable.toString());
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean close() throws Exception {

    // todo retry for a while if unable
    for (Initializable initializable : serviceModule.getInitializables()) {
      if (!initializable.close()) {
        log.error("Unable to close " + initializable.toString());
        return false;
      }
    }
    return true;
  }
}
