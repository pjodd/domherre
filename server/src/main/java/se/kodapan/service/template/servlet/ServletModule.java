package se.kodapan.service.template.servlet;

import com.google.common.reflect.ClassPath;
import com.google.inject.Provides;
import org.gwizard.rest.RestModule;
import org.gwizard.swagger.SwaggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kalle
 * @since 2017-03-07 22:39
 */
public class ServletModule extends com.google.inject.servlet.ServletModule {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected void configureServlets() {
    filter("/*").through(TrackingFilter.class);
    install(new RestModule());
    bindPathAnnotatedClasses();
  }

  private void bindPathAnnotatedClasses() {
    ClassLoader cl = getClass().getClassLoader();
    Set<ClassPath.ClassInfo> classesInPackage;

    Set<Package> pathPackages = new HashSet<>();

    try {
      classesInPackage = ClassPath.from(cl).getTopLevelClassesRecursive("se.kodapan.service");
    } catch (IOException ioe) {
      log.error("Exception caught during search for @Path-annotated classes", ioe);
      throw new RuntimeException(ioe);
    }
    for (ClassPath.ClassInfo classInfo : classesInPackage) {
      Class pathAnnotatedClass;
      try {
        pathAnnotatedClass = classInfo.load();
      } catch (NoClassDefFoundError e) {
//        log.debug("Exception while inspecting class " + classInfo.getName(), e);
        continue;
      }
      if (pathAnnotatedClass.isAnnotationPresent(Path.class)) {
        log.info("Binding @Path-annotated class " + pathAnnotatedClass.getName());
        bind(pathAnnotatedClass);
        pathPackages.add(pathAnnotatedClass.getPackage());
      }
    }

    // configure swagger
    List<String> resourcePackages = new ArrayList<>(pathPackages.size());
    for (Package pack : pathPackages) {
      log.info("Registering Swagger resource package {}", pack.getName());
      resourcePackages.add(pack.getName());
    }
    swaggerConfig = new SwaggerConfig();
    swaggerConfig.setPrettyPrint(true);
    swaggerConfig.setBasePath("/");
    swaggerConfig.setResourcePackages(resourcePackages);

  }

  private SwaggerConfig swaggerConfig;

  @Provides
  public SwaggerConfig swaggerConfig() {
    return swaggerConfig;
  }

}
