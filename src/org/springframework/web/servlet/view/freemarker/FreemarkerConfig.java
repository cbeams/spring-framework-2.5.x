/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.freemarker;

import freemarker.template.Configuration;


/**
 * Interface to be implemented by objects that configure
 * and manage a FreeMarker Configuration object.
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id: FreemarkerConfig.java,v 1.1 2004-03-11 20:02:26 davison Exp $
 */
public interface FreemarkerConfig {
	
	/**
	 * Return a <code>Configuration</code> object for FreeMarker that
	 * may be used to set FreeMarker properties and shared objects, and
	 * from which <code>Template</code>'s may be retrieved.
	 * @return a FreeMarker <code>Configuration</code> object
	 * @see FreemarkerView
	 */
	Configuration getConfiguration();
	
}
