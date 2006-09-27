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

package org.springframework.web.portlet.handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.springframework.beans.BeansException;
import org.springframework.util.Assert;

/**
 * Abstract base class for HandlerMapping implementations that rely on a map
 * which caches handler objects per lookup key. Supports arbitrary lookup keys,
 * and automatically resolves handler bean names into handler bean instances.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #getLookupKey(javax.portlet.PortletRequest)
 * @see #registerHandler(Object, Object)
 */
public abstract class AbstractMapBasedHandlerMapping extends AbstractHandlerMapping {

	private boolean lazyInitHandlers = false;

	private final Map handlerMap = new HashMap();


	/**
	 * Set whether to lazily initialize handlers. Only applicable to
	 * singleton handlers, as prototypes are always lazily initialized.
	 * Default is false, as eager initialization allows for more efficiency
	 * through referencing the handler objects directly.
	 * <p>If you want to allow your handlers to be lazily initialized,
	 * make them "lazy-init" and set this flag to true. Just making them
	 * "lazy-init" will not work, as they are initialized through the
	 * references from the handler mapping in this case.
	 */
	public void setLazyInitHandlers(boolean lazyInitHandlers) {
		this.lazyInitHandlers = lazyInitHandlers;
	}


	/**
	 * Determines a handler for the computed lookup key for the given request.
	 * @see #getLookupKey
	 */
	protected Object getHandlerInternal(PortletRequest request) throws Exception {
		Object lookupKey = getLookupKey(request);
		Object handler = this.handlerMap.get(lookupKey);
		if (handler != null && logger.isDebugEnabled()) {
			logger.debug("Key [" + lookupKey + "] -> handler [" + handler + "]");
		}
		return handler;
	}

	/**
	 * Build a lookup key for the given request.
	 * @param request current portlet request
	 * @return the lookup key (never <code>null</code>)
	 * @throws Exception if key computation failed
	 */
	protected abstract Object getLookupKey(PortletRequest request) throws Exception;


	/**
	 * Register all handlers specified in the Portlet mode map for the corresponding modes.
	 * @param handlerMap Map with lookup keys as keys and handler beans or bean names as values
	 * @throws BeansException if the handler couldn't be registered
	 */
	protected void registerHandlers(Map handlerMap) throws BeansException {
		Assert.notNull(handlerMap, "Handler Map must not be null");
		for (Iterator it = handlerMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			registerHandler(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Register the given handler instance for the given parameter value.
	 * @param lookupKey the key to map the handler onto
	 * @param handler the handler instance or handler bean name String
	 * (a bean name will automatically be resolved into the corrresponding handler bean)
	 * @throws BeansException if the handler couldn't be registered
	 * @throws IllegalStateException if there is a conflicting handler registered
	 */
	protected void registerHandler(Object lookupKey, Object handler) throws BeansException, IllegalStateException {
		Assert.notNull(lookupKey, "Lookup key must not be null");
		Assert.notNull(handler, "Handler object must not be null");

		// Check for duplicate mapping.
		Object mappedHandler = this.handlerMap.get(lookupKey);
		if (mappedHandler != null) {
			throw new IllegalStateException("Cannot map handler [" + handler + "] to key [" + lookupKey +
					"]: There's already handler [" + mappedHandler + "] mapped.");
		}

		// Eagerly resolve handler if referencing singleton via name.
		if (!this.lazyInitHandlers && handler instanceof String) {
			String handlerName = (String) handler;
			if (getApplicationContext().isSingleton(handlerName)) {
				handler = getApplicationContext().getBean(handlerName);
			}
		}

		// Add the handler to the map.
		this.handlerMap.put(lookupKey, handler);
		if (logger.isDebugEnabled()) {
			logger.debug("Mapped key [" + lookupKey + "] onto handler [" + handler + "]");
		}
	}

}
