/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.io.Resource;

/** 
 * Interface to provide configuration for an application.
 * This is read-only while the application is running,
 * but may be reloaded if the implementation supports this.
 *
 * <p>The configuration provides:
 * <ul>
 * <li>The ability to resolve messages, supporting internationalization.
 * <li>The ability to publish events. Implementations must provide a means
 * of registering event listeners.
 * <li>Bean factory methods, inherited from ListableBeanFactory. This
 * avoids the need for applications to use singletons.
 * <li>Notification of beans initialized by the context of the context,
 * enabling communication with the rest of the application, for
 * example by publishing events. The BeanFactory superinterface
 * provides no similar mechanism.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has its
 * own child context that is independent of that of any other servlet.
 * </ul>
 *
 * @author Rod Johnson
 * @version $Id: ApplicationContext.java,v 1.12 2004-02-02 11:46:38 jhoeller Exp $
 */
public interface ApplicationContext extends ListableBeanFactory, HierarchicalBeanFactory, MessageSource {
	
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
	 */
	void publishEvent(ApplicationEvent event);

	/**
	 * Return a Resource handle for the specified resource.
	 * The handle should always be a reusable resource descriptor,
	 * allowing for multiple getInputStream calls.
	 * <p><ul>
	 * <li>Must support fully qualified URLs, e.g. "file:C:/test.dat".
	 * <li>Must support classpath pseudo-URLs, e.g. "classpath:test.dat".
	 * <li>Should support relative file paths, e.g. "WEB-INF/test.dat".
	 * </ul>
	 * <p>Note that a Resource handle does not imply an existing resource;
	 * you need to invoke Resource's "exists" to check for existence.
	 * @param location resource location
	 * @return Resource handle
	 * @see org.springframework.core.io.Resource#exists
	 * @see org.springframework.core.io.Resource#getInputStream
	 * @see org.springframework.core.io.ResourceEditor#CLASSPATH_URL_PREFIX
	 */
	Resource getResource(String location);

}
