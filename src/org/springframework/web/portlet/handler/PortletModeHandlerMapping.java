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
import java.util.Properties;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;

/**
 * <p>Implementation of the HandlerMapping interface to map from
 * the current PortletMode to request handler beans.</p>
 *
 * <p>The bean configuration for this mapping will look something like this:</p>
 * <pre>
 * 	&lt;bean id="portletModeHandlerMapping" class="org.springframework.web.portlet.handler.PortletModeHandlerMapping"&gt;
 * 		&lt;property name="portletModeMap"&gt;
 * 			&lt;map&gt;
 * 				&lt;entry key="view"&gt;&lt;ref bean="viewHandler"/&gt;&lt;/entry&gt;
 * 				&lt;entry key="edit"&gt;&lt;ref bean="editHandler"/&gt;&lt;/entry&gt;
 * 				&lt;entry key="help"&gt;&lt;ref bean="helpHandler"/&gt;&lt;/entry&gt;
 * 			&lt;/map&gt;
 * 		&lt;/property&gt;
 * 	&lt;/bean&gt;
 * </pre>
 *
 * @author William G. Thompson, Jr.
 * @author John A. Lewis
 * @since 2.0
 */
public class PortletModeHandlerMapping extends AbstractHandlerMapping {

	private Map portletModeMap;

	private boolean lazyInitHandlers = false;

	private final Map handlerMap = new HashMap();


	/**
	 * Set a Map with PortletModes as keys and handler beans as values.
	 * Convenient for population with bean references.
	 * @param portletModeMap map with PortletModes as keys and beans as values
	 */
	public void setPortletModeMap(Map portletModeMap) {
		this.portletModeMap = portletModeMap;
	}

	/**
	 * Set PortletMode to handler bean name mappings from a Properties object.
	 * @param mappings properties with PortletMode.toString() as key and bean name as value
	 */
	public void setMappings(Properties mappings) {
		this.portletModeMap = mappings;
	}

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


	public void initApplicationContext() throws BeansException {
		// Make sure the map got initialized.
		if (this.portletModeMap == null || this.portletModeMap.isEmpty()) {
			if (logger.isWarnEnabled()) {
				logger.warn("Neither 'portletModeMap' nor 'mappings' set on PortletModeHandlerMapping");
			}
		}
		else {
			// Iterate through the portlet modes in the passed in map.
			for (Iterator it = this.portletModeMap.keySet().iterator(); it.hasNext();) {
				// Get the portlet mode for this mapping.
				String modeKey = (String) it.next();
				PortletMode mode = new PortletMode(modeKey);
				// Get the handler and register it.
				Object handler = this.portletModeMap.get(modeKey);
				registerHandler(mode, handler);
			}
		}
	}

	/**
	 * Register the given handler instance for the given PortletMode.
	 * @param mode the PortletMode the bean is mapped to
	 * @param handler the handler instance
	 * @throws BeansException if the handler couldn't be registered
	 */
	protected void registerHandler(PortletMode mode, Object handler)
			throws BeansException {

		// check for duplicate mapping
		Object mappedHandler = this.handlerMap.get(mode);
		if (mappedHandler != null)
			throw new ApplicationContextException("Cannot map handler [" + handler + "] to mode [" + mode +
					"]: there's already handler [" + mappedHandler + "] mapped");

		// eagerly resolve handler if referencing singleton via name
		if (!this.lazyInitHandlers && handler instanceof String) {
			String handlerName = (String) handler;
			if (getApplicationContext().isSingleton(handlerName)) {
				handler = getApplicationContext().getBean(handlerName);
			}
		}

		// add the handler to the map
		this.handlerMap.put(mode, handler);
		logger.info("Mapped mode [" + mode + "] onto handler [" + handler + "]");
	}

	/**
	 * Look up a handler for the PortletMode of the given request.
	 * @param request current portlet request
	 * @return the looked up handler instance, or <code>null</code>
	 */
	protected Object getHandlerInternal(PortletRequest request) throws Exception {
		// Look up the handler for the portlet mode.
		PortletMode mode = request.getPortletMode();
		Object handler = this.handlerMap.get(mode);
		if (logger.isDebugEnabled()) {
			logger.debug("Portlet mode '" + mode + "' -> handler [" + handler + "]");
		}
		return handler;
	}

}
