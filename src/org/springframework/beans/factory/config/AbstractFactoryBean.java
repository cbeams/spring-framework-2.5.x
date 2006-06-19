/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;

/**
 * Simple template superclass for {@link FactoryBean}
 * implementations that creates a singleton or a prototype object,
 * depending on a flag.
 *
 * <p>If the "singleton" flag is <code>true</code>  (the default),
 * this class will create the object that it creates exactly once
 * on initialization and subsequently return said singleton instance
 * on all calls to the {@link #getObject()} method.
 * 
 * <p>Else, this class will create a new instance every time the
 * {@link #getObject()} method is invoked. Subclasses are responsible
 * for implementing the abstract {@link #createInstance()} template
 * method to actually create the object(s) to expose.
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @since 1.0.2
 * @see #setSingleton(boolean)
 * @see #createInstance()
 */
public abstract class AbstractFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean singleton = true;

	private boolean initialized = false;

	private Object singletonInstance;


	/**
	 * Set if a singleton should be created, or a new object
	 * on each request else. Default is <code>true</code>  (a singleton).
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public boolean isSingleton() {
		return singleton;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.singleton) {
			this.initialized = true;
			this.singletonInstance = createInstance();
		}
	}


	public final Object getObject() throws Exception {
		if (this.singleton) {
			if (!this.initialized) {
				throw new FactoryBeanNotInitializedException();
			}
			return this.singletonInstance;
		}
		else {
			return createInstance();
		}
	}

	public void destroy() throws Exception {
		if (this.singleton) {
			destroyInstance(this.singletonInstance);
		}
	}


	/**
	 * Template method that subclasses must override to construct
	 * the object returned by this factory.
	 * <p>Invoked on initialization of this FactoryBean in case of
	 * a singleton; else, on each {@link #getObject()} call.
	 * @return the object returned by this factory
	 * @throws Exception if an exception occured during object creation
	 * @see #getObject()
	 */
	protected abstract Object createInstance() throws Exception;

	/**
	 * Callback for destroying a singleton instance. Subclasses may
	 * override this to destroy the previously created instance.
	 * <p>The default implementation is empty.
	 * @param instance the singleton instance, as returned by
	 * {@link #createInstance()}
	 * @throws Exception in case of shutdown errors
	 * @see #createInstance()
	 */
	protected void destroyInstance(Object instance) throws Exception {
	}

}
