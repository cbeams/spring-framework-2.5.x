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

package org.springframework.beans.factory.wiring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

/**
 * Convenient superclass for configurers that can perform Dependency Injection on
 * objects (however they may be created). Typically subclassed by AspectJ aspects.

 * <p>Subclasses may also need a metadata resolution strategy, in the
 * {@link BeanWiringInfoResolver} interface. The default implementation looks
 * for a bean with the same name as the fully-qualified class name. (This is
 * the default name of the bean in a Spring XML file if the id attribute is
 * not used.)

 * @author Rob Harrop
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 2.0
 */
public abstract class BeanConfigurerSupport implements BeanFactoryAware, DisposableBean  {

	/** Logger available to subclasses */
	protected Log logger = LogFactory.getLog(getClass());


	private BeanWiringInfoResolver beanWiringInfoResolver = new ClassNameBeanWiringInfoResolver();

	private AutowireCapableBeanFactory beanFactory;


	/**
	 * Set the BeanWiringInfoResolver to use. Default behavior will be to look
	 * for a bean with the same name as the class.
	 * <p>As an alternative, consider using annotation-driven bean wiring.
	 * @see ClassNameBeanWiringInfoResolver
	 * @see org.springframework.beans.factory.annotation.AnnotationBeanWiringInfoResolver
	 */
	public void setBeanWiringInfoResolver(BeanWiringInfoResolver beanWiringInfoResolver) {
		Assert.notNull(beanWiringInfoResolver, "beanWiringInfoResolver is required");
		this.beanWiringInfoResolver = beanWiringInfoResolver;
	}

	/**
	 * DI the Spring application context in which this aspect should configure beans.
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof AutowireCapableBeanFactory)) {
			throw new IllegalArgumentException(
				 "Bean configurer aspect needs to run in an AutowireCapableBeanFactory, not in [" + beanFactory + "]");
		}
		this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
	}

	/**
	 * Release references to BeanFactory and BeanWiringInfoResolver when
	 * application context is destroyed
	 */
	public void destroy() {
		this.beanFactory = null;
		// do not set beanWiringInfoResolver to null, allowing repeated injection with
		// different application context instances
		//this.beanWiringInfoResolver = null;
	}


	/**
	 * Configure the bean instance using the given bean name.
	 * Subclasses can override this to provide custom configuration logic.
	 * <p>Typically called by an aspect, for all bean instances matched
	 * by a pointcut.
	 * @param beanInstance the bean instance to configure (must <b>not</b> be <code>null</code>
	 */
	protected void configureBean(Object beanInstance) {
		BeanWiringInfo bwi = this.beanWiringInfoResolver.resolveWiringInfo(beanInstance);
		if (bwi == null) {
			// Skip the bean if no wiring info given.
			return;
		}

		if (this.beanFactory == null) {
			if(logger.isWarnEnabled()) {
				logger.warn("BeanFactory has not been set on [" + getClass().getName() + "]: " +
					"Make sure this configurer runs in a Spring container. " +
					"For example, add it to a Spring application context as an XML bean definition.");
			}
			return;
		}

		if (bwi.indicatesAutowiring()) {
			// Perform autowiring.
			this.beanFactory.autowireBeanProperties(beanInstance, bwi.getAutowireMode(), bwi.getDependencyCheck());
		}
		else {
			// Perform explicit wiring.
			this.beanFactory.applyBeanPropertyValues(beanInstance, bwi.getBeanName());
		}
	}

}
