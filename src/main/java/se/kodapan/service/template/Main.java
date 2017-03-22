package se.kodapan.service.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kalle
 * @since 2017-03-22
 */
public class Main {

  public static void main(String[] args) throws Exception {

    Logger log = LoggerFactory.getLogger(Main.class);

    // todo insert your service implementation class here
    Service service = new Service();

    if (service.open()) {
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          try {
            service.close();
          } catch (Exception e) {
            log.error("Exception while closing service", e);
          }
        }
      });

    }

  }

}
