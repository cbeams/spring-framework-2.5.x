/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.mvc.multiaction;

import java.util.Iterator;
import java.util.Properties;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.PathMatcher;

/**
 * The most sophisticated and useful framework implementation of 
 * the MethodNameResolver interface. Uses java.util.Properties
 * defining the mapping between the URL of incoming requests and
 * method name. Such properties can be held in an XML document.
 *
 * <p>Properties format is
 * <code>
 * /welcome.html=displayGenresPage
 * </code>
 * Note that method overloading isn't allowed, so there's no
 * need to specify arguments.
 *
 * <p>Supports direct matches, e.g. a registered "/test" matches "/test",
 * and a various Ant-style pattern matches, e.g. a registered "/t*" matches
 * both "/test" and "/team". For details, see the PathMatcher class.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.util.Properties
 * @see org.springframework.util.PathMatcher
 */
public class PropertiesMethodNameResolver extends AbstractUrlMethodNameResolver
		implements InitializingBean {
	
	private Properties mappings;

	/**
	 * Set URL to method name mappings from a Properties object.
	 * @param mappings properties with URL as key and method name as value
	 */
	public void setMappings(Properties mappings) {
		this.mappings = mappings;
	}
	
	public void afterPropertiesSet() {
		if (this.mappings == null || this.mappings.isEmpty()) {
			throw new IllegalArgumentException("'mappings' property is required");
		}
	}

	protected String getHandlerMethodNameForUrlPath(String urlPath) {
		String name = this.mappings.getProperty(urlPath);
		if (name != null) {
			return name;
		}
		for (Iterator it = this.mappings.keySet().iterator(); it.hasNext();) {
			String registeredPath = (String) it.next();
			if (PathMatcher.match(registeredPath, urlPath)) {
				return (String) this.mappings.get(registeredPath);
			}
		}
		return null;
	}

}
