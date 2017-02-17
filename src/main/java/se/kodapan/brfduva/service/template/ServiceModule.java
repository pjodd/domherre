package se.kodapan.brfduva.service.template;

import com.google.inject.Binder;
import com.google.inject.Module;

import java.util.Collection;
import java.util.Collections;

/**
 * @author kalle
 * @since 2017-02-15 02:28
 */
public class ServiceModule implements Module {

  @Override
  public void configure(Binder binder) {

  }

  public Collection<Initializable> getInitializables() {
    return Collections.EMPTY_LIST;
  }

}
