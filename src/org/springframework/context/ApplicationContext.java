/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.io.ResourceLoader;

/** 
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running,
 * but may be reloaded if the implementation supports this.
 *
 * <p>The configuration provides:
 * <ul>
 * <li>Bean factory methods, inherited from ListableBeanFactory. This
 * avoids the need for applications to use singletons.
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the MessageSource interface.
 * <li>The ability to publish events. Implementations must provide a means
 * of registering event listeners.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: ApplicationContext.java,v 1.13 2004-03-12 12:34:10 jhoeller Exp $
 */
public interface ApplicationContext
    extends ListableBeanFactory, HierarchicalBeanFactory, MessageSource, ResourceLoader {
	
	/**
	 * Return the parent context, or null if there is no parent,
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or null if there is no parent
	 */
	ApplicationContext getParent();
	
	/**
	 * Return a friendly name for this context.
	 * @return a display name for this context
	*/
	String getDisplayName();

	/**
	 * Return the timestamp when this context was first loaded.
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();

	/**
	 * Notify all listeners registered with this application of an application
	 * event. Events may be framework events (such as RequestHandledEvent)
	 * or application-specific events.
	 * @param event event to publish
	 * @see org.springframework.web.context.support.RequestHandledEvent
	 */
	void publishEvent(ApplicationEvent event);

}
