/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.freemarker;

import freemarker.template.Configuration;

/**
 * Interface to be implemented by objects that configure and manage a
 * FreeMarker Configuration object in a web environment. Detected and
 * used by FreemarkerView.
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id: FreemarkerConfig.java,v 1.2 2004-03-14 21:40:04 jhoeller Exp $
 * @see FreemarkerConfigurer
 * @see FreemarkerView
 */
public interface FreemarkerConfig {
	
	/**
	 * Return the FreeMarker <code>Configuration</code> object for the current
	 * web application context.
	 * <p>A FreeMarker Configuration object may be used to set FreeMarker
	 * properties and shared objects, and allows to retrieve <code>Template</code>s.
	 * @return the FreeMarker Configuration
	 */
	Configuration getFreemarkerConfiguration();
	
}
