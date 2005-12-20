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

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

/**
 * Class holding wiring metadata information about a bean definition.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanWiringInfo {
	
	/**
	 * Constant that indicates autowiring bean properties by name.
	 * @see #BeanWiringInfo(int, boolean)
	 */
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * Constant that indicates autowiring bean properties by type.
	 * @see #BeanWiringInfo(int, boolean)
	 */
	int AUTOWIRE_BY_TYPE = 2;


	private String beanName = null;

	private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;
	
	private boolean dependencyCheck = false;


	/**
	 * Create a new BeanWiringInfo that points to the given bean name.
	 * @param beanName the name of the bean definition to take the property values from
	 */
	public BeanWiringInfo(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanName = beanName;
	}
	
	/**
	 * Create a new BeanWiringInfo that indicates autowiring.
	 * @param autowireMode either constant AUTOWIRE_BY_NAME or AUTOWIRE_BY_TYPE
	 * @param dependencyCheck whether to perform a dependency check for object
	 * references in the bean instance (after autowiring)
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#AUTOWIRE_BY_NAME
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#AUTOWIRE_BY_TYPE
	 */
	public BeanWiringInfo(int autowireMode, boolean dependencyCheck) {
		this.autowireMode = autowireMode;
		this.dependencyCheck = dependencyCheck;
	}


	/**
	 * Return whether this BeanWiringInfo indicates autowiring.
	 */
	public boolean indicatesAutowiring() {
		return (this.beanName == null);
	}

	/**
	 * Return the specific bean name that this BeanWiringInfo points to, if any.
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * Return the constant AUTOWIRE_BY_NAME or AUTOWIRE_BY_TYPE, if autowiring is indicated.
	 */
	public int getAutowireMode() {
		return autowireMode;
	}

	/**
	 * Return whether to perform a dependency check for object references
	 * in the bean instance (after autowiring).
	 */
	public boolean getDependencyCheck() {
		return dependencyCheck;
	}

}
