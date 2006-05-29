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
import org.springframework.context.ApplicationContextException;

/**
 * <p>Implementation of the HandlerMapping interface to map from
 * a request parameter to request handler beans.</p>
 * 
 * <p>The default name of the parameter is "action", but can be changed using
 * {@link #setParameterName setParameterName()}.</p>
 *
 * <p>The bean configuration for this mapping will look somthing like this:</p>
 * <pre>
 * 	&lt;bean id="parameterHandlerMapping" class="org.springframework.web.portlet.handler.ParameterHandlerMapping"&gt;
 *		&lt;property name="parameterMap"&gt;
 *			&lt;map&gt;
 *				&lt;entry key="add"&gt;&lt;ref bean="addItemHandler"/&gt;&lt;/entry&gt;
 *				&lt;entry key="edit"&gt;&lt;ref bean="editItemHandler"/&gt;&lt;/entry&gt;
 *				&lt;entry key="delete"&gt;&lt;ref bean="deleteItemHandler"/&gt;&lt;/entry&gt;
 *			&lt;/map&gt;
 *		&lt;/property&gt;
 *	&lt;/bean&gt;
 * </pre>
 * 
 * @author Rainer Schmitz
 * @author John A. Lewis
 * @since 2.0
 * @see ParameterMappingInterceptor
 */
public class ParameterHandlerMapping extends AbstractHandlerMapping {

    // request parameter name to use for mapping to handlers
    public final static String DEFAULT_PARAMETER_NAME = "action";


	private String parameterName = DEFAULT_PARAMETER_NAME;

	private Map parameterMap;

	private boolean lazyInitHandlers = false;

	protected final Map handlerMap = new HashMap();


    /**
     * Set the name of the parameter used for mapping.
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Get the name of the parameter used for mapping.
     */
    public String getParameterName() {
        return parameterName;
    }

	/**
	 * Set a Map with parameters as keys and handler beans as values.
	 * Convenient for population with bean references.
	 * @param parameterMap map with parameters as keys and beans as values
	 */
	public void setParameterMap(Map parameterMap) {
		this.parameterMap = parameterMap;
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

	    // make sure parameterName has a value
		if (this.getParameterName() == null)
            throw new IllegalArgumentException("A parameterName is required");
	
	    // make sure the map got initialized
	    if (this.parameterMap == null || this.parameterMap.isEmpty())
			logger.warn("Neither 'parameterMap' nor 'mappings' set on ParameterHandlerMapping");
		else {

		    // iterate through the entries in the passed in map
		    for(Iterator itr = this.parameterMap.keySet().iterator(); itr.hasNext(); ) {

	            // get the parameter value for this mapping
		        String parameter = (String)itr.next();

				// get the handler and register it
				Object handler = this.parameterMap.get(parameter);
				registerHandler(parameter, handler);
			}
		}
	}
	
    /**
	 * Register the given handler instance for the given parameter value.
	 * @param parameter the parameter value to which this handler is mapped
	 * @param handler the handler instance bean
	 * @throws BeansException if the handler couldn't be registered
	 */
	protected void registerHandler(String parameter, Object handler) 
			throws BeansException {

	    // check for duplicate mapping
	    Object mappedHandler = this.handlerMap.get(parameter);
		if (mappedHandler != null)
			throw new ApplicationContextException("Cannot map handler [" + handler + "] to parameter value [" + parameter +
			        "]: there's already handler [" + mappedHandler + "] mapped");

		// eagerly resolve handler if referencing singleton via name
		if (!this.lazyInitHandlers && handler instanceof String) {
			String handlerName = (String) handler;
			if (getApplicationContext().isSingleton(handlerName)) {
				handler = getApplicationContext().getBean(handlerName);
			}
		}

		// add the handler to the map
		this.handlerMap.put(parameter, handler);
		logger.info("Mapped parameter value [" + parameter + "] onto handler [" + handler + "]");
	}

	/**
	 * Look up a handler for the parameter value of the given request.
	 * @param request current portlet request
	 * @return the looked up handler instance, or null
	 */
	protected Object getHandlerInternal(PortletRequest request) throws Exception {

	    // get the parameter value
	    String parameter = request.getParameter(this.getParameterName());

	    // look up the handler for the given parameter value
	    Object handler = this.handlerMap.get(parameter);
	    if (logger.isDebugEnabled())
			logger.debug("parameter [" + parameter + "] = " + "handler [" + handler + "]");

	    // return the handler
	    return handler;
	}

}
