package se.kodapan.brfduva.service.template;

import com.google.inject.*;
import org.gwizard.services.Run;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.brfduva.service.template.prevalence.MessageQueuePrevalence;
import se.kodapan.brfduva.service.template.rest.ServletModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author kalle
 * @since 2017-02-12 22:14
 */
public class Service {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Injector injector;

  public void run() {
    injector.getInstance(Run.class).start();
  }

  public boolean open() throws Exception {

    List<Module> modules = new ArrayList<>();
    modules.addAll(getAdditionalModules());
    modules.add(new ServletModule());
    injector = Guice.createInjector(modules);


    {
      List<Initializable> unopnenedInitializables = new ArrayList<>();
      unopnenedInitializables.add(injector.getInstance(MessageQueuePrevalence.class));

      for (Class<? extends Initializable> initializableClass : getInitializables()) {
        Initializable initializable = injector.getInstance(initializableClass);
        unopnenedInitializables.add(initializable);
      }

      List<Initializable> openendInitializables = new ArrayList<>();
      long millisecondsTimeOut = TimeUnit.MINUTES.toMillis(1);
      long started = System.currentTimeMillis();
      while (!unopnenedInitializables.isEmpty()) {

        long millisecondsSpent = System.currentTimeMillis() - started;
        if (millisecondsSpent > millisecondsTimeOut) {
          log.error("Timeout opening initializables.");
          // todo close any that we opened

          return false;
        }

        for (Iterator<Initializable> iterator = unopnenedInitializables.iterator(); iterator.hasNext(); ) {
          Initializable initializable = iterator.next();
          if (initializable.open()) {
            iterator.remove();
            openendInitializables.add(initializable);
            log.info("Initialized " + initializable);
          } else {
            log.warn("Unable to initialize " + initializable);
          }
        }
      }

      long ended = System.currentTimeMillis();
      long millisecondsSpent = ended - started;
      log.info(millisecondsSpent + " milliseconds spent opening all initializables.");
    }

    return true;
  }

  public boolean close() throws Exception {

//    // todo retry for a while if unable
//    for (Initializable initializable : serviceModule.getInitializables()) {
//      if (!initializable.close()) {
//        log.error("Unable to close " + initializable.toString());
//        return false;
//      }
//    }

    return true;
  }

  public List<Class<? extends Initializable>> getInitializables() {
    return Collections.emptyList();
  }

  public List<Module> getAdditionalModules() {
    return Collections.emptyList();
  }
}
