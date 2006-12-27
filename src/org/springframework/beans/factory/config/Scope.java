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

import org.springframework.beans.factory.ObjectFactory;

/**
 * Strategy interface used by a {@link ConfigurableBeanFactory},
 * representing a target scope to hold beans in.
 *
 * <p>Provides the ability to get and put objects from whatever underlying
 * storage mechanism, such as HTTP session or request. The name passed into
 * this class's <code>get</code> and <code>put</code> methods will identify
 * the target attribute in the scope.
 *
 * <p><code>Scope</code> implementations are expected to be thread-safe.
 * One <code>Scope</code> can be used with multiple bean factories, if desired.
 *
 * <p>An example of a possible underlying storage mechanism would be a session
 * API such as the Servlet API's {@link javax.servlet.http.HttpSession} interface.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see ConfigurableBeanFactory#registerScope
 * @see org.springframework.aop.scope.ScopedProxyFactoryBean
 * @see CustomScopeConfigurer
 */
public interface Scope {

	/**
	 * Return the conversation id for the current underlying scope, if any.
	 * <p>The exact value of the converation id is totally dependent on the
	 * underlying storage mechanism (of course). In the case of session scoped
	 * beans a good conversation id probably would be the
	 * {@link javax.servlet.http.HttpSession#getId() session id}; in the case
	 * of a JCache scope a good conversation id probably would be the name of
	 * the associated cache.
	 * <p>It is perfectly valid to return <code>null</code> in an
	 * implementation of this method if the underlying storage mechanism has
	 * no obvious good candidate for a conversation id.
	 * @return the conversation id, or <code>null</code> if there is no
	 * conversation id for this scope
	 */
	String getConversationId();

	/**
	 * Return the object with the given name from the underlying scope,
	 * {@link org.springframework.beans.factory.ObjectFactory#getObject() creating it}
	 * if not found in the underlying storage mechanism.
	 * @param name the name of the object to retrieve
	 * @param objectFactory the {@link ObjectFactory} to use to create the scoped object if it is not present in the
	 * underlying storage mechanism
	 * @return the desired object
	 */
	Object get(String name, ObjectFactory objectFactory);

	/**
	 * Remove the object with the given <code>name</code> from the underlying scope.
	 * <p>Returns <code>null</code> if no object was found; otherwise
	 * returns the removed <code>Object</code>.
	 * @param name the name of the object to remove
	 * @return the removed object, if any
	 */
	Object remove(String name);

	/**
	 * Register a callback to be executed at destruction of the specified
	 * object (or at destruction of the entire scope, if the scope does not
	 * destroy individual objects but rather only terminate in its entirety).
	 * <p>Implementations should do their best to execute the callback
	 * at the appropriate time. If such a callback is not supported
	 * by the underlying runtime environment, the callback <i>must be
	 * ignored</i> and a corresponding warning should be logged.
	 * @param name the name of the object to execute the destruction callback for
	 * @param callback the destruction callback to be executed
	 */
	void registerDestructionCallback(String name, Runnable callback);

}
