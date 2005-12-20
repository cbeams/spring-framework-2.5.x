/*
 * Copyright 2002-2005 the original author or authors.
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
 
package org.springframework.beans.factory.aspectj;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Abstract superaspect for AspectJ aspects that can perform Dependency Injection on
 * objects, however they may be created. Define the beanCreation() pointcut
 * in subaspects.

 * <p>Subaspects may also need a metadata resolution strategy, in the BeanWiringInfoResolver
 * interface. The default implementation looks for a bean with the same name as the
 * FQN. This is the default name of the bean in a Spring XML file if the id
 * attribute is not used.

 * @author Rob Harrop
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
public abstract aspect AbstractBeanConfigurer implements BeanFactoryAware {

	private BeanWiringInfoResolver beanWiringInfoResolver = new ClassNameBeanWiringInfoResolver();

	private ConfigurableListableBeanFactory beanFactory;

	
	/**
	 * Set a custom BeanWiringInfoResolver. Default behaviour will be to look
	 * for a bean with the same name as the class.
	 */
	public void setBeanWiringInfoResolver(BeanWiringInfoResolver beanWiringInfoResolver) {
		this.beanWiringInfoResolver = beanWiringInfoResolver;
	}
    
	/**
	 * DI the Spring application context in which this aspect should configure beans.
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
 				"Bean configurer aspect needs to run in a ConfigurableListableBeanFactory, not in [" + beanFactory + "]");
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}


	/**
	 * Configure the bean instance using the given bean name. Sub-aspects can
	 * override to provide custom configuration logic.
	 */
	protected void configureBean(Object bean, BeanWiringInfo bwi) {
		if (this.beanFactory == null) {
			throw new IllegalStateException(
					"BeanFactory has not be set on aspect [" + this.getClass().getName() + "]: " +
					"This aspect should normally be added to a Spring container, for example in an XML bean definition");
		}

		if (bwi.indicatesAutowiring()) {
			// Perform autowiring.
			this.beanFactory.autowireBeanProperties(bean, bwi.getAutowireMode(), bwi.getDependencyCheck());
		}
		else {
			// Perform explicit wiring.
			this.beanFactory.applyBeanPropertyValues(bean, bwi.getBeanName());
		}
	}


	/**
	 * The creation of a new bean (an object with the @Configurable annotation)
	 */
	protected abstract pointcut beanCreation(Object beanInstance);


	/**
	 * All beans should be configured after construction.
	 */
	after(Object beanInstance) returning : beanCreation(beanInstance) {
		BeanWiringInfo bwi = beanWiringInfoResolver.resolveWiringInfo(beanInstance);
		if (bwi != null) {
			configureBean(beanInstance, bwi);
		}
	}

}
