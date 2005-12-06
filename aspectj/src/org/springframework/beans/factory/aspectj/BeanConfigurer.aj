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
 
package org.springframework.beans.factory.aspectj;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author Adrian Colyer
 * @author Rob Harrop
 */
public aspect BeanConfigurer implements BeanFactoryAware {

	private ConfigurableListableBeanFactory beanFactory;
    
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
     * The creation of a new bean (an object with the @SpringConfigured annotation)
     */
    pointcut beanCreation(SpringConfigured beanAnnotation, Object beanInstance) :
        initialization((@SpringConfigured *).new(..)) &&
        @this(beanAnnotation) &&
        this(beanInstance);

    /**
     * All beans should be configured after construction.
     */
    after(SpringConfigured beanAnnotation, Object beanInstance) returning :
      beanCreation(beanAnnotation,beanInstance) {
      configureBean(beanInstance, getBeanName(beanAnnotation,beanInstance));
    }
    
    
    /**
     * The bean name is either the value given in the annotation (@SpringConfigured("MyBean") ),
     * or the name of the type if no value is given (@SpringConfigured ).
     */
    private String getBeanName(SpringConfigured beanAnnotation, Object beanInstance) {
        String beanName = beanAnnotation.value();
        if ("".equals(beanName)) beanName = beanInstance.getClass().getName();     
        return beanName;
    }

    /**
     * Configure the bean instance using the given bean name. Sub-aspects can
     * override to provide custom configuration logic.
     */
    protected void configureBean(Object bean,String beanName) {;
        this.beanFactory.applyBeanPropertyValues(bean, beanName);
    }
}
