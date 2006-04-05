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

package org.springframework.aop.target.scope;

/**
 * Strategy interface used by the ScopedTargetSource class.
 * Provides the ability to get and put objects from whatever underlying
 * storage mechanism, such as HTTP Session or Request. The scope
 * identifier passed in to this class's get and put methods will
 * identifier the scope in which the map applies.
 *
 * <p>ScopeMaps are expected to be threadsafe. One ScopeMap
 * can be used with multiple ScopedTargetSources.
 *
 * <p>A ThreadLocal
 * strategy may be used to populate this. Alternatively the implementation
 * may look at the current proxy. If the proxy config's exposeProxy
 * flag is set to true, the proxy will have been bound to the thread
 * before the TargetSource and ScopeMap are invoked.
 *
 * <p>Can be implemented on top of a session API such as the
 * Servlet API's HttpSession interface.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.target.scope.ScopedTargetSource
 * @see javax.servlet.http.HttpSession
 */
public interface ScopeMap {

	/**
	 * Is this scope persistent? Can we reconnect to objects from it?
	 * @return whether or not this scope is persistent, meaning
	 * that the handle will be usable to reconnect to the object
	 */
	boolean isPersistent();

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
