/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;

/**
 * Implementation of the HandlerMapping interface to map from URLs to
 * request handler beans. Supports both mapping to bean instances and
 * mapping to bean names: The latter is required for prototype handlers.
 *
 * <p>The "urlMap" property is suitable for populating the handler map
 * with bean references, e.g. via the map element in XML bean definitions.
 *
 * <p>Mappings to bean names can be set via the "mappings" property, in a
 * form accepted by the java.util.Properties class, like as follows:<br>
 * <code>
 * /welcome.html=ticketController
 * /show.html=ticketController
 * </code><br>
 * The syntax is PATH=HANDLER_BEAN_NAME.
 * If the path doesn't begin with a slash, one is prepended.
 *
 * <p>Supports direct matches (given "/test" -> registered "/test")
 * and "*" matches (given "/test" -> registered "/t*").
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see java.util.Properties
 */
public class SimpleUrlHandlerMapping extends AbstractUrlHandlerMapping {
	
	private Map urlMap;

	/**
	 * Set a Map with URLs as keys and handler beans as values.
	 * Convenient for population with bean references.
	 * @param urlMap map with URLs as keys and beans as values
	 */
	public void setUrlMap(Map urlMap) {
		this.urlMap = urlMap;
	}

	/**
	 * Set URL to handler bean name mappings from a Properties object.
	 * @param mappings properties with URL as key and bean name as value
	 */
	public void setMappings(Properties mappings) {
		this.urlMap = mappings;
	}

	public void initApplicationContext() throws BeansException {
		if (this.urlMap == null || this.urlMap.isEmpty()) {
			logger.info("Neither 'urlMap' nor 'mappings' set on SimpleUrlHandlerMapping");
		}
		else {
			Iterator itr = this.urlMap.keySet().iterator();
			while (itr.hasNext()) {
				String url = (String) itr.next();
				Object handler = this.urlMap.get(url);
				// prepend with slash if it's not present
				if (!url.startsWith("/")) {
					url = "/" + url;
				}
				registerHandler(url, handler);
			}
		}
	}

}
