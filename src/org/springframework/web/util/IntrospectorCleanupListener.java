package org.springframework.web.util;

import java.beans.Introspector;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Listener that flushes the JavaBeans Introspector cache on web app shutdown.
 * Register this listener in your web.xml to guarantee proper release of
 * the web app class loader and the classes that it holds.
 *
 * <p>If the JavaBeans Introspector has been used to analyze application classes,
 * the Introspector cache will hold a hard reference to those classes.
 * Consequently, those classes and the web app class loader will not be
 * garbage collected on web app shutdown!
 *
 * <p>Unfortunately, the only way to clean up the Introspector is to flush
 * the entire cache, as there is no way to specifically determine the
 * application's classes referenced there. This will remove cached
 * introspection results for all other applications in the server too.
 *
 * <p>Note that this listener is <i>not</i> necessary when using Spring's
 * beans infrastructure, as Spring's own introspection results cache will
 * immediately flush an analyzed class from the JavaBeans Introspector cache.
 *
 * <p>Application classes hardly ever need to use the JavaBeans Introspector
 * directly, so are normally not the cause of Introspector resource leaks.
 * Rather, many libraries and frameworks do not clean up the Introspector,
 * for example Struts and Quartz.
 *
 * <p>Note that a single such Introspector leak will cause the entire web
 * app class loader to not get garbage collected! This has the consequence that
 * you will see all the application's static class resources (like singletons)
 * around after web app shutdown, which is not the fault of those classes!
 *
 * @author Juergen Hoeller
 * @since 05.06.2004
 * @see java.beans.Introspector#flushCaches
 */
public class IntrospectorCleanupListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
	}

	public void contextDestroyed(ServletContextEvent event) {
		Introspector.flushCaches();
	}

}
