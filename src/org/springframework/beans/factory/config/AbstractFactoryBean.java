/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.beans.factory.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Simple template superclass for FactoryBean implementations thats allows
 * for creating a singleton or a prototype, depending on a flag.
 *
 * <p>If the "singleton" flag is "true" (the default), this class will
 * create once on initialization and subsequently return the singleton
 * instance. Else, this class will create a new instance each time.
 * Subclasses are responsible for implementing the abstract
 * <code>createInstance</code> template method to actually create objects.
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @since 25.04.2004
 */
public abstract class AbstractFactoryBean implements FactoryBean, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private boolean singleton = true;

	private Object singletonInstance;

	/**
	 * Set if a singleton should be created, or a new object
	 * on each request else. Default is true.
	 */
	public final void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public final boolean isSingleton() {
		return singleton;
	}

	public final void afterPropertiesSet() throws Exception {
		if (singletonInstance == null) {
			this.singletonInstance = createInstance();
		}
	}

	public final Object getObject() throws Exception {
		if (isSingleton()) {
			return this.singletonInstance;
		}
		else {
			return createInstance();
		}
	}

	/**
	 * Template method that subclasses must override to construct
	 * the object returned by this factory.
	 * <p>Invoked on initialization of this FactoryBean in case of
	 * a singleton; else, on each getObject() call.
	 * @return the object returned by this factory
	 * @throws Exception if an exception occured during object creation
	 * @see #getObject
	 */
	protected abstract Object createInstance() throws Exception;

}
