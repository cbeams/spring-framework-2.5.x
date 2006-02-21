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

package org.springframework.beans.factory;

/**
 * Exception thrown if a FactoryBean is not fully initialized, for example
 * because it is involved in a circular reference. Usually indicated by
 * the <code>getObject</code> method returning <code>null</code>.
 *
 * <p>A circular reference with a FactoryBean cannot be solved by eagerly
 * caching singleton instances like with normal beans. The reason is that
 * <i>every</i> FactoryBean needs to be fully initialized before it can
 * return the created bean, while only <i>specific</i> normal beans need
 * to be initialized - that is, if a collaborating bean actually invokes
 * them on initialization instead of just storing the reference.
 *
 * @author Juergen Hoeller
 * @since 30.10.2003
 * @see FactoryBean#getObject
 */
public class FactoryBeanNotInitializedException extends BeanCreationException {

	/**
	 * Create a new FactoryBeanNotInitializedException.
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 */
	public FactoryBeanNotInitializedException(String beanName, String msg) {
		super(beanName, msg);
	}

}
