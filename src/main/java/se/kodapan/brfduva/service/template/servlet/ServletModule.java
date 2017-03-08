package se.kodapan.brfduva.service.template.servlet;

import com.google.common.reflect.ClassPath;
import org.gwizard.rest.RestModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Set;

/**
 * @author kalle
 * @since 2017-03-07 22:39
 */
public class ServletModule extends com.google.inject.servlet.ServletModule {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected void configureServlets() {
    install(new RestModule());

    bindPathAnnotatedClasses();
  }

  private void bindPathAnnotatedClasses() {
    ClassLoader cl = getClass().getClassLoader();
    Set<ClassPath.ClassInfo> classesInPackage;

    try {
      classesInPackage = ClassPath.from(cl).getTopLevelClassesRecursive("se.kodapan.brfduva.service");
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
      }
    }

  }
}
