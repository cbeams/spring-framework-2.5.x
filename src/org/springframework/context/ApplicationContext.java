/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.io.ResourceLoader;

/** 
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.
 *
 * <p>An ApplicationContext provides:
 * <ul>
 * <li>Bean factory methods, inherited from ListableBeanFactory.
 * This avoids the need for applications to use singletons.
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the MessageSource interface.
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the ResourceLoader interface.
 * <li>The ability to publish events. Implementations must provide a means
 * of registering event listeners.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 *
 * <p>In addition to standard bean factory lifecycle capabilities,
 * ApplicationContext implementations need to detect ApplicationContextAware
 * beans and invoke the setApplicationContext method accordingly.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: ApplicationContext.java,v 1.15 2004-03-22 10:34:50 jhoeller Exp $
 * @see ApplicationContextAware#setApplicationContext
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
