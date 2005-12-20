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

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * Class holding wiring metadata information about a bean definition.
 * 
 * @author Rod Johnson
 */
public class BeanWiringInfo {
	
	private final String beanName;
	
	private final int autowireMode;
	
	private final boolean dependencyCheck;
	
	public BeanWiringInfo(String beanName) {
		this(beanName, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
	}
	
	public BeanWiringInfo(int autowireMode, boolean dependencyCheck) {
		this(null, autowireMode, dependencyCheck);
	}
	
	private BeanWiringInfo(String beanName, int autowireMode, boolean dependencyCheck) {
		this.beanName = beanName;
		this.autowireMode = autowireMode;
		this.dependencyCheck = dependencyCheck;
	}
	
	public boolean hasBeanName() {
		return beanName != null;
	}

	public String getBeanName() {
		return beanName;
	}
	
	public int getAutowireMode() {
		return autowireMode;
	}
	
	public boolean getDependencyCheck() {
		return dependencyCheck;
	}

}
