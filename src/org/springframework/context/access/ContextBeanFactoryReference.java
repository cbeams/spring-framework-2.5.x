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

package org.springframework.context.access;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * <p>
 * ApplicationContext-specific implementation of BeanFactoryReference,
 * wrapping a newly created ApplicationContext, closing it on release.
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
public class ContextBeanFactoryReference implements BeanFactoryReference {

	private ApplicationContext applicationContext;

	public ContextBeanFactoryReference(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public BeanFactory getFactory() {
		ApplicationContext retval = applicationContext;
		if (retval == null)
			throw new IllegalStateException(
					"ApplicationContext owned by this BeanFactoryReference has been released");
		return retval;
	}

	public void release() {
		
		if (applicationContext != null) {
			ApplicationContext savedCtx;
			
			// we don't actually guarantee thread-safty, but it's not a lot of extra work
			synchronized (this) {
				savedCtx = applicationContext;
				applicationContext = null;
			}

			if (savedCtx != null && savedCtx instanceof ConfigurableApplicationContext) {
				((ConfigurableApplicationContext) savedCtx).close();
			}
		}
	}
}
