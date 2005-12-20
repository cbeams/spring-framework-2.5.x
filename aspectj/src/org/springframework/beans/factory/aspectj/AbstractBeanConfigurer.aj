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
 
package org.springframework.beans.factory.aspectj;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Abstract superaspect for AspectJ aspects that can perform Dependency Injection on
 * objects, however they may be created. Define the beanCreation() pointcut
 * in subaspects.
 * <p>
 * Subaspects may also need a metadata resolution strategy, in the BeanWiringInfoResolver
 * interface. The default implementation looks for a bean with the same name as the
 * FQN. This is the default name of the bean in a Spring XML file if the id
 * attribute is not used.
 * @author Rob Harrop
 * @author Rod Johnson
 * @author Adrian Colyer
 * @since 2.0
 */
public abstract aspect AbstractBeanConfigurer implements BeanFactoryAware {

	/**
	 * Owning bean factory
	 */
	private ConfigurableListableBeanFactory beanFactory;
	
	private BeanWiringInfoResolver beanWiringInfoResolver = BeanWiringInfoResolver.CLASSNAME_WIRING_INFO_RESOLVER;
	
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
    	if(!(beanFactory instanceof ConfigurableListableBeanFactory)) {
    		throw new IllegalArgumentException("Must run in a ConfigurableListableBeanFactory.");
    	}
    	this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }
	/**
     * The creation of a new bean. Subaspects are responsible for
     * matching strategy, which may include an annotation but may match
     * existing objects, and may work without requiring Java 5 or AspectJ 5.
     */
    protected abstract pointcut beanCreation(Object beanInstance);

    /**
     * All beans should be configured after construction.
     */
    after(Object beanInstance) returning :
      beanCreation(beanInstance) {
    	BeanWiringInfo bwi = beanWiringInfoResolver.resolve(beanInstance);
    	if (bwi != null) {
    		configureBean(beanInstance, bwi);
    	}
    }

    /**
     * Configure the bean instance using the given bean name. Sub-aspects can
     * override to provide custom configuration logic.
     */
    private void configureBean(Object bean, BeanWiringInfo bwi) {
    	if (this.beanFactory == null) {
    		throw new IllegalStateException("BeanFactory has not be set on aspect " + this.getClass().getName() + ": " +
    				"This aspect should normally be added to a Spring container, for example in an XML bean definition");
    	}
    	
    	if (bwi.hasBeanName()) {
    		// Do explicit wiring
    		this.beanFactory.applyBeanPropertyValues(bean, bwi.getBeanName());
    	}
    	else {
    		// Do autowire
    		if (this.beanFactory instanceof AutowireCapableBeanFactory) {
    			AutowireCapableBeanFactory aacbf = (AutowireCapableBeanFactory) this.beanFactory;
    			aacbf.autowireBeanProperties(bean, bwi.getAutowireMode(), bwi.getDependencyCheck());
    		}
    		else {
    			throw new IllegalArgumentException("Cannot autowire with factory " + this.beanFactory + "; " +
    					"Found autowire annotation on class " + bean.getClass().getName());
    		}
    	}
    }
}
