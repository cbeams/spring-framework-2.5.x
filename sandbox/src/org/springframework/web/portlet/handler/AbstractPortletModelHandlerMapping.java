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

package org.springframework.web.portlet.handler;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;

/**
 * Abstract base class for PortletMode-mapped HandlerMapping implementations.
 * Provides infrastructure for mapping handlers to PortletModes.
 *
 * @author William G. Thompson, Jr.
 */
public abstract class AbstractPortletModelHandlerMapping extends AbstractHandlerMapping {

	private boolean lazyInitHandlers = false;

	private final Map handlerMap = new HashMap();

	/**
	 * Set whether to lazily initialize handlers. Only applicable to
	 * singleton handlers, as prototypes are always lazily initialized.
	 * Default is false, as eager initialization allows for more efficiency
	 * through referencing the controller objects directly.
	 * <p>If you want to allow your controllers to be lazily initialized,
	 * make them "lazy-init" and set this flag to true. Just making them
	 * "lazy-init" will not work, as they are initialized through the
	 * references from the handler mapping in this case.
	 */
	public void setLazyInitHandlers(boolean lazyInitHandlers) {
		this.lazyInitHandlers = lazyInitHandlers;
	}

	/**
	 * Look up a handler for the PortletMode of the given request.
	 * @param request current portlet request
	 * @return the looked up handler instance, or null
	 */
	protected Object getHandlerInternal(PortletRequest request) throws Exception {
	    PortletMode portletMode = request.getPortletMode();
	    // TODO get portlet name from PortletConfig via PortletApplicationObjectSupport??
	    String portletName = "TODO portletName";
		logger.debug("Looking up handler for [" + portletName + "] in [" + portletMode + "] PortletMode");
		return handlerMap.get(portletMode.toString());
	}

	/**
	 * Register the given handler instance for the given PortletMode.
	 * @param String PortletMode the bean is mapped to
	 * @param handler the handler instance
	 * @throws BeansException if the handler couldn't be registered
	 */
	protected void registerHandler(String portletMode, Object handler) throws BeansException {
		Object mappedHandler = this.handlerMap.get(portletMode);
		if (mappedHandler != null) {
			throw new ApplicationContextException("Cannot map handler [" + handler + "] to PortletMode [" + portletMode +
			                                      "]: there's already handler [" + mappedHandler + "] mapped");
		}

		// eagerly resolve handler if referencing singleton via name
		if (!this.lazyInitHandlers && handler instanceof String) {
			String handlerName = (String) handler;
			if (getApplicationContext().isSingleton(handlerName)) {
				handler = getApplicationContext().getBean(handlerName);
			}
		}

		this.handlerMap.put(portletMode, handler);
		logger.info("Mapped PortletMode [" + portletMode + "] onto handler [" + handler + "]");
	}

}
