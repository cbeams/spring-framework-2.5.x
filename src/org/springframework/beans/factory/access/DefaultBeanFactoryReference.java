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

package org.springframework.beans.factory.access;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * <pDefault implementation of BeanFactoryReference, wrapping a newly created
 * BeanFactory, destroying its singletons on release.
 * </p>
 * 
 * <p>
 * As per BeanFactoryReference contract, release may be called more than once,
 * with subsequent calls not doing anything. However, callging getFactory after
 * a release call will cause an exception.
 * </p>
 * 
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @since 13.02.2004
 */
public class DefaultBeanFactoryReference implements BeanFactoryReference {

	private BeanFactory beanFactory;

	public DefaultBeanFactoryReference(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public BeanFactory getFactory() {
		BeanFactory retval = this.beanFactory;
		if (retval == null)
			throw new IllegalStateException(
					"BeanFactory owned by this BeanFactoryReference has been released");
		return retval;
	}

	public void release() {

		if (beanFactory != null) {
			BeanFactory savedFactory;

			// we don't actually guarantee thread-safty, but it's not a lot of extra work
			synchronized (this) {
				savedFactory = beanFactory;
				beanFactory = null;
			}

			if (savedFactory != null && savedFactory instanceof ConfigurableBeanFactory) {
				((ConfigurableBeanFactory) savedFactory).destroySingletons();
			}
		}
	}
}
