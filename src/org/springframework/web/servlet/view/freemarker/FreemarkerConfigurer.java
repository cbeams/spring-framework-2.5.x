/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.freemarker;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.ui.freemarker.ConfigurationFactory;


/**
 * JavaBean to configure FreeMarker for web usage, via the "configLocation" and/or
 * "freemarkerProperties".  If neither of them is set, the default config location 
 * "/WEB-INF/freemarker.properties" will be used if available.
 *
 * <p>This bean must be included in the application context of any application
 * using Spring's FreemarkerView for web MVC. It exists purely to configure FreeMarker.
 * It is not meant to be referenced by application components but just internally
 * by FreemarkerView. Implements FreemarkerConfig to be found by FreemarkerView without
 * depending on the bean name the configurer. Each DispatcherServlet can define its
 * own FreemarkerConfigurer if desired.
 *
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id: FreemarkerConfigurer.java,v 1.1 2004-03-11 20:02:26 davison Exp $
 * @see #setConfigLocation
 * @see #setFreemarkerProperties
 * @see FreemarkerView
 */
public class FreemarkerConfigurer extends ConfigurationFactory
		implements ApplicationContextAware, FreemarkerConfig {

	public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/freemarker.properties";
			
	private ApplicationContext applicationContext;

	
    /**
     * Sets a reference to the Spring application context and initializes the
     * FreeMarker configuration.
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		initConfiguration();
    }

	/**
	 * Returns a <code>Resource</code> describing the default properties (settings)
	 * that will be applied to the FreeMArker configuration if the Resource exists.
	 * @see org.springframework.ui.freemarker.ConfigurationFactory#getDefaultConfigLocation()
	 */
	protected Resource getDefaultConfigLocation() {
		return this.applicationContext.getResource(DEFAULT_CONFIG_LOCATION);
	}

}
