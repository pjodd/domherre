package se.kodapan.service.template.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kalle
 * @since 2017-02-22 00:29
 */
public class Environment {

  /** Mainly for unit tests */
  private static Map<String, String> defaultValues = new HashMap<>();

  public static void setDefaultValue(String key, String value) {
    defaultValues.put(key, value);
  }
  
  public static String getValue(String key, String defaultValue) {
    String value = System.getenv(key);
    if (value == null) {
      value = defaultValues.get(key);
    }
    return value != null ? value : defaultValue;
  }


}
