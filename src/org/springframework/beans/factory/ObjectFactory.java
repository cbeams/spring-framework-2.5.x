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
 * <p>Interface defining a factory which can return an Object instance
 * (possibly shared or independent) when invoked.
 * 
 * <p>This interface is typically used to encapsulate a generic factory 
 * which returns a new instance (prototype) on each invocation.
 * It is similar to FactoryBean, but implementations of the latter interface
 * are normally meant to be defined as instances by the user in a BeanFactory,
 * while implementations of this class are normally meant to be fed as a
 * property to other beans. As such, the getObject method has different
 * exception handling behavior.
 * 
 * @author Colin Sampaleanu
 * @since 2004-5-11
 * @see FactoryBean
 */
public interface ObjectFactory {

	/**
	 * Return an instance (possibly shared or independent)
	 * of the object managed by this factory.
	 * @return an instance of the bean (should never be null)
	 * @throws BeansException in case of creation errors
	 */
	Object getObject() throws BeansException;

}
