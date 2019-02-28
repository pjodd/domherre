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

  /**
   * All modules, including all service modules.
   */
  private List<Module> modules;

  /**
   * Modules aware of service name, containing initializable configurations, etc
   */
  private List<AbstractServiceModule> serviceModules;

  @Getter
  private Injector injector;

  public void run() {
    injector.getInstance(Run.class).start();
  }

  public void stop() {
    injector.getInstance(Run.class).stop();
  }

  public boolean open() throws Exception {

    modules = new ArrayList<>();

    modules.add(new ServiceModule(serviceName));

    modules.add(new ServletModule());
    modules.add(new SwaggerModule()); // depends on actions in ServletModule. binds to /swagger.json

    for (Module module : getModules()) {
      modules.add(module);
    }

    serviceModules = new ArrayList<>();
    for (Module module : modules) {
      if (module instanceof AbstractServiceModule) {
        serviceModules.add((AbstractServiceModule) module);
      }
    }

    injector = Guice.createInjector(modules);

    List<Initializable> unopnenedInitializables = new ArrayList<>();

    for (AbstractServiceModule serviceModule : serviceModules) {
      for (Class<? extends Initializable> initializableClass : serviceModule.getInitializables()) {
        Initializable initializable = injector.getInstance(initializableClass);
        unopnenedInitializables.add(initializable);
      }
    }

    List<Initializable> openendInitializables = new ArrayList<>();
    long millisecondsTimeOut = TimeUnit.MINUTES.toMillis(1);
    long started = System.currentTimeMillis();
    while (!unopnenedInitializables.isEmpty()) {

      long millisecondsSpent = System.currentTimeMillis() - started;
      if (millisecondsSpent > millisecondsTimeOut) {
        log.error("Timeout opening initializables. Unable to open {}", unopnenedInitializables);

        // close any that we opened
        if (!openendInitializables.isEmpty()) {
          log.info("Closing any initializables that was opened...");
          for (Initializable initializable : openendInitializables) {
            if (!initializable.close()) {
              log.error("Unable to close " + initializable.toString());
            }
          }
        }

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


    return true;
  }

  public boolean close() throws Exception {

    List<Initializable> notClosedInitializables = new ArrayList<>();
    for (AbstractServiceModule serviceModule : serviceModules) {
      for (Class<? extends Initializable> initializableClass : serviceModule.getInitializables()) {
        notClosedInitializables.add(injector.getInstance(initializableClass));
      }
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

  public List<Module> getModules() {
    return Collections.emptyList();
  }


}
