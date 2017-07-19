package se.kodapan.service.template.mq.localfs;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import javax.inject.Singleton;
import java.io.File;

/**
 * @author kalle
 * @since 2017-07-19
 */
public class LocalFsMessageQueueModule implements Module {

  private String absoluteRootPath;

  public LocalFsMessageQueueModule() {
    this(new File("data/localfsmq").getAbsolutePath());
  }

  public LocalFsMessageQueueModule(String absoluteRootPath) {
    this.absoluteRootPath = absoluteRootPath;
  }


  @Override
  public void configure(Binder binder) {

  }

  @Provides
  @Named("local fs message queue absolute root path")
  public final String localFsMessageQueueAbsoluteRootPathProvider() {
    return absoluteRootPath;
  }


}
