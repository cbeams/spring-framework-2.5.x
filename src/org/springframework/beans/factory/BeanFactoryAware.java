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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * Interface to be implemented by beans that wish to be aware of their owning
 * BeanFactory. Beans can e.g. look up collaborating beans via the factory.
 *
 * <p>Note that most beans will choose to receive references to collaborating
 * beans via respective bean properties.
 *
 * <p>For a list of all bean lifecycle methods, see the BeanFactory javadocs.
 *
 * @author Rod Johnson
 * @since 11-Mar-2003
 * @version $Revision: 1.5 $
 * @see BeanNameAware
 * @see InitializingBean
 * @see BeanFactory
 * @see org.springframework.context.ApplicationContextAware
 */
public interface BeanFactoryAware {
	
	/**
	 * Callback that supplies the owning factory to a bean instance.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * @param beanFactory owning BeanFactory (may not be null).
	 * The bean can immediately call methods on the factory.
	 * @throws BeansException in case of initialization errors
	 * @see BeanInitializationException
	 */
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
