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

package org.springframework.web.portlet.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;

/**
 *  PortletMode - mapped PortletControllerMapping implementation
 * Provides infrastructure for mapping controllers to PortletModes.
 *
 * @author William G. Thompson, Jr.
 */
public class PortletModeControllerMapping extends AbstractPortletControllerMapping {

	private boolean lazyInitControllers = false;

	private Map portletModeMap;
	private final Map controllerMap = new HashMap();

	/**
	 * Set a Map with PortletModes as keys and controller beans as values.
	 * Convenient for population with bean references.
	 * @param portletModeMap map with PortletModes as keys and beans as values
	 */
	public void setPortletModeMap(Map portletModeMap) {
		this.portletModeMap = portletModeMap;
	}

	/**
	 * Set PortletMode to controller bean name mappings from a Properties object.
	 * @param mappings properties with PortletMode.toString() as key and bean name as value
	 */
	public void setMappings(Properties mappings) {
		this.portletModeMap = mappings;
	}

	public void initApplicationContext() throws BeansException {
		if (this.portletModeMap == null || this.portletModeMap.isEmpty()) {
			logger.info("Neither 'portletModeMap' nor 'mappings' set on PortletModeControllerMapping");
		}
		else {
			Iterator itr = this.portletModeMap.keySet().iterator();
			while (itr.hasNext()) {
				String portletMode = (String) itr.next();
				Object controller = this.portletModeMap.get(portletMode);
				registerController(portletMode, controller);
			}
		}
	}
	
	/**
	 * Set whether to lazily initialize controllers. Only applicable to
	 * singleton controllers, as prototypes are always lazily initialized.
	 * Default is false, as eager initialization allows for more efficiency
	 * through referencing the controller objects directly.
	 * <p>If you want to allow your controllers to be lazily initialized,
	 * make them "lazy-init" and set this flag to true. Just making them
	 * "lazy-init" will not work, as they are initialized through the
	 * references from the controller mapping in this case.
	 */
	public void setLazyInitHandlers(boolean lazyInitControllers) {
		this.lazyInitControllers = lazyInitControllers;
	}

	/**
	 * Look up a controller for the PortletMode of the given request.
	 * @param request current portlet request
	 * @return the looked up controller instance, or null
	 */
	protected PortletController getPortletControllerInternal(PortletRequest request) throws Exception {
	    PortletMode portletMode = request.getPortletMode();
	    // TODO get portlet name from PortletConfig via PortletApplicationObjectSupport??
	    String portletName = "TODO portletName";
		logger.debug("Looking up controller for [" + portletName + "] in [" + portletMode + "] PortletMode");
		return (PortletController) controllerMap.get(portletMode.toString());
	}

	/**
	 * Register the given controller instance for the given PortletMode.
	 * @param String PortletMode the bean is mapped to
	 * @param controller the controller instance
	 * @throws BeansException if the controller couldn't be registered
	 */
	protected void registerController(String portletMode, Object controller) throws BeansException {
		Object mappedController = this.controllerMap.get(portletMode);
		if (mappedController != null) {
			throw new ApplicationContextException("Cannot map controller [" + controller + "] to PortletMode [" + portletMode +
			                                      "]: there's already controller [" + mappedController + "] mapped");
		}

		// eagerly resolve controller if referencing singleton via name
		if (!this.lazyInitControllers && controller instanceof String) {
			String controllerName = (String) controller;
			if (getApplicationContext().isSingleton(controllerName)) {
				controller = getApplicationContext().getBean(controllerName);
			}
		}

		this.controllerMap.put(portletMode, controller);
		logger.info("Mapped PortletMode [" + portletMode + "] onto controller [" + controller + "]");
	}

}
