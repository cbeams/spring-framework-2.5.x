/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.context.event;

import java.util.Collection;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.core.CollectionFactory;

/**
 * Abstract implementation of the {@link ApplicationEventMulticaster} interface,
 * providing the basic listener registration facility.
 *
 * <p>Doesn't permit multiple instances of the same listener by default,
 * as it keeps listeners in a linked Set. The collection class used to hold
 * ApplicationListener objects can be overridden through the "collectionClass"
 * bean property.
 *
 * <p>Note that this class doesn't try to do anything clever to ensure thread
 * safety if listeners are added or removed at runtime. A technique such as
 * Copy-on-Write (Lea:137) could be used to ensure this, but the assumption in
 * the basic version of the class is that listeners will be added at application
 * configuration time and not added or removed as the application runs.
 *
 * <p>A custom collection class must be specified to allow for thread-safe
 * runtime registration of listeners. A good candidate for this is Doug Lea's
 * <code>java.util.concurrent.CopyOnWriteArraySet</code> or its non-JDK predecessor,
 * <code>EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArraySet</code> (or the
 * respective CopyOnWriteArrayList version, allowing for registering the same
 * listener multiple times). Those classes provide a thread-safe Iterator,
 * optimized for read-mostly usage - matching this use case nicely.
 *
 * <p>Implementing ApplicationEventMulticaster's actual {@link #multicastEvent} method
 * is left to subclasses. {@link SimpleApplicationEventMulticaster} simply multicasts
 * all events to all registered listeners, invoking them in the calling thread.
 * Alternative implementations could be more sophisticated in those respects.
 *
 * @author Juergen Hoeller
 * @since 1.2.3
 * @see #setCollectionClass
 * @see #getApplicationListeners()
 * @see SimpleApplicationEventMulticaster
 */
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster {

	/** Collection of ApplicationListeners */
	private Collection applicationListeners = CollectionFactory.createLinkedSetIfPossible(16);


	/**
	 * Specify the collection class to use. Can be populated with a fully
	 * qualified class name when defined in a Spring application context.
	 * <p>Default is a linked HashSet, keeping the registration order.
	 * If no linked Set implementation is available, a plain HashSet will
	 * be used as fallback (not keeping the registration order).
	 * <p>Note that a Set class specified will not permit multiple instances
	 * of the same listener, while a List class will allow for registering
	 * the same listener multiple times.
	 * <p>Consider Doug Lea's <code>java.util.concurrent.CopyOnWriteArraySet</code> or its
	 * JDK 1.4 backport, <code>edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet</code>
	 * (or the respective CopyOnWriteArrayList version). Those classes provide a thread-safe
	 * Iterator, optimized for read-mostly usage - matching this use case nicely.
	 * @see org.springframework.core.CollectionFactory#createLinkedSetIfPossible
	 * @see java.util.concurrent.CopyOnWriteArraySet
	 * @see edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet
	 */
	public void setCollectionClass(Class collectionClass) {
		if (collectionClass == null) {
			throw new IllegalArgumentException("'collectionClass' must not be null");
		}
		if (!Collection.class.isAssignableFrom(collectionClass)) {
			throw new IllegalArgumentException("'collectionClass' must implement [java.util.Collection]");
		}
		// Create desired collection instance.
		Collection newColl = (Collection) BeanUtils.instantiateClass(collectionClass);
		// Add all previously registered listeners (usually none).
		newColl.addAll(this.applicationListeners);
		this.applicationListeners = newColl;
	}


	public void addApplicationListener(ApplicationListener listener) {
		this.applicationListeners.add(listener);
	}

	public void removeApplicationListener(ApplicationListener listener) {
		this.applicationListeners.remove(listener);
	}

	public void removeAllListeners() {
		this.applicationListeners.clear();
	}

	/**
	 * Return the current Collection of ApplicationListeners.
	 * <p>Note that this is the raw Collection of ApplicationListeners,
	 * potentially modified when new listeners get registered or
	 * existing ones get removed. This Collection is not a snapshot copy.
	 * @return a Collection of ApplicationListeners
	 * @see org.springframework.context.ApplicationListener
	 */
	protected Collection getApplicationListeners() {
		return this.applicationListeners;
	}

}
