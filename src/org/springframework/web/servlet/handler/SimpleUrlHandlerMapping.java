/**
 * Generic framework code included with 
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002). 
 * This code is free to use and modify. However, please
 * acknowledge the source and include the above URL in each
 * class using or derived from this code. 
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package org.springframework.web.servlet.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of the HandlerMapping interface to map from URLs
 * to request handler beans.
 *
 * <p>The "urlMap" property is suitable for populating the map values
 * as bean references, e.g. via the map element in XML bean definitions.
 *
 * <p>Mappings can also be set via the "mappings" property, in a form
 * accepted by the java.util.Properties class, like as follows:<br>
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
 * @see org.springframework.web.servlet.DispatcherServlet
 * @author Rod Johnson
 * @author Juergen Hoeller
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

	public void initApplicationContext() {
		if (this.urlMap == null) {
			throw new IllegalArgumentException("Either 'urlMap' or 'mappings' is required");
		}
		
		if (!this.urlMap.isEmpty()) {
			Iterator itr = this.urlMap.keySet().iterator();
			while (itr.hasNext()) {
				String url = (String) itr.next();
				Object handler = this.urlMap.get(url);

				if (handler instanceof String) {
					// convert bean name (assumably from "mappings") to bean instance
					handler = getApplicationContext().getBean((String) handler);
				}
				initHandler(handler, url);

				if ("*".equals(url)) {
					// register default handler
					if (getDefaultHandler() != null) {
						throw new IllegalArgumentException("Cannot apply * mapping: default handler is already set");
					}
					setDefaultHandler(handler);
				}
				else {
					// register specific handler
					// prepend with slash if it's not present
					if (!url.startsWith("/")) {
						url = "/" + url;
					}
					registerHandler(url, handler);
				}
			}
		}
		else {
			logger.warn("No mappings defined in " + getClass().getName());
		}
	}

}
