package se.kodapan.brfduva.service.template;

/**
 * @author kalle
 * @since 2017-02-12 22:14
 */
public class Service {

  private static Service instance = new Service();

  public static Service getInstance() {
    return instance;
  }

  private Service() {

  }

  public void open() throws Exception {
    
  }

  public void close() throws Exception {

  }

}
