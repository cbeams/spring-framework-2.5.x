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

package org.springframework.beans.factory.config;

/**
 * Strategy interface used by a ConfigurableBeanFactory,
 * representing a target scope to hold beans in.
 *
 * <p>Provides the ability to get and put objects from whatever underlying
 * storage mechanism, such as HTTP session or request. The name passed into
 * this class's <code>get</code> and <code>put</code> methods will identify
 * the target attribute in the scope.
 *
 * <p>ScopeMaps are expected to be thread-safe. One ScopeMap
 * can be used with multiple bean factories, if desired.
 *
 * <p>Can be implemented on top of a session API such as the
 * Servlet API's HttpSession interface.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see ConfigurableBeanFactory#registerScope
 * @see org.springframework.aop.scope.ScopedProxyFactoryBean
 * @see javax.servlet.http.HttpSession
 */
public interface ScopeMap {

	/**
	 * Return the object from the underlying scope,
	 * or <code>null</code> if not found.
	 * @param name the name to bind with
	 * @return object the associated value, or <code>null</code>
	 */
	Object get(String name);
	
	/**
	 * Bind the object to the underlying scope.
	 * @param name the name to bind with
	 * @param value the object to bind
	 */
	void put(String name, Object value);

	/**
	 * Remove the object with the given name from the underlying scope.
	 * @param name the name of the object to remove
	 */
	void remove(String name);

}
