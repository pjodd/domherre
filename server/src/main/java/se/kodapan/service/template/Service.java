package se.kodapan.service.template;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.Getter;
import org.gwizard.services.Run;
import org.gwizard.swagger.SwaggerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.servlet.ServletModule;

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

  @Getter
  private String serviceName;

  public Service(String serviceName) {
    this.serviceName = serviceName;
  }

  @Getter
  private Injector injector;

  public void run() {
    injector.getInstance(Run.class).start();
  }

  public void stop() {
    injector.getInstance(Run.class).stop();
  }

  public boolean open() throws Exception {

    List<Module> modules = new ArrayList<>();

    modules.add(new ServiceModule(serviceName));

    modules.add(new ServletModule());
    modules.add(new SwaggerModule()); // depends on actions in ServletModule. binds to /swagger.json

    modules.addAll(getModules());

    injector = Guice.createInjector(modules);


    {
      List<Initializable> unopnenedInitializables = new ArrayList<>();

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

    List<Initializable> notClosedInitializables = new ArrayList<>();
    for (Class<? extends Initializable> initializableClass : getInitializables()) {
      notClosedInitializables.add(injector.getInstance(initializableClass));
    }

    // todo retry for a while if unable

    boolean success = true;

    for (Initializable initializable : notClosedInitializables) {
      if (!initializable.close()) {
        log.error("Unable to close " + initializable.toString());
        success = false;
      }
    }

    return success;
  }

  public List<Class<? extends Initializable>> getInitializables() {
    return Collections.emptyList();
  }

  public List<Module> getModules() {
    return Collections.emptyList();
  }


}
