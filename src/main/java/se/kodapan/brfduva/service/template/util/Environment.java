package se.kodapan.brfduva.service.template.util;

/**
 * @author kalle
 * @since 2017-02-22 00:29
 */
public class Environment {

  public static String getValue(String key, String defaultValue) {
    String value = System.getenv(key);
    return value != null ? value : defaultValue;
  }

}
