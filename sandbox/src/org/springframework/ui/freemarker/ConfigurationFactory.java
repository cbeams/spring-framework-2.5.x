/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ui.freemarker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;


/**
 * Factory that configures a FreeMarker Configuration. Can be used standalone, but
 * typically you will either use ConfigurationFactoryBean for preparing a
 * Configuration as bean reference, or FreemarkerConfigurer for web views.
 *
 * <p>The optional "configLocation" property sets the location of a FreeMarker
 * properties file, within the current application. FreeMarker properties can be
 * overridden via "freemarkerSettings".  All of these properties will be set by
 * calling FreeMarker's Configuration.setSetting() method and are subject to
 * constraints set by FreeMarker
 *
 * <p>The "freemarkerVariables" property can be used to specify a Map of shared
 * variables that will be applied to the Configuration via the setSharedVariable()
 * method.  Like setSettings(), these entries are subject to FreeMarker constraints
 * and may throw a FreemarkerInitializationException.
 *
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id: ConfigurationFactory.java,v 1.1 2004-03-05 19:45:18 davison Exp $
 */
public class ConfigurationFactory {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private Resource configLocation;

	private Properties freemarkerSettings;
	
	private Map freemarkerVariables;

	private Configuration configuration;
	
	private boolean isCustomConfiguration = false;


	/**
	 * allows a custom FreeMarker Configuration object to be set which 
	 * will be returned with all subsequent calls to getConfiguration().
	 * A ConfigurationFactory populated with a custom configuration will not
	 * attempt to initialize or modify the properties of that configuration - 
	 * the client code must ensure that the configuration is correct first. 
	 * @param configuration
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
		isCustomConfiguration = (configuration != null);
	}
	
    /**
     * @return
     */
    public synchronized Configuration getConfiguration() {
		if (this.configuration == null)
			initConfiguration();
		return this.configuration;
	}

    /**
     * @param resource
     */
    public void setConfigLocation(Resource resource) {
        configLocation = resource;
    }

    /**
     * @param properties a Properties file containing well-known FreeMarker keys
     * which will be passed to FreeMarker's Configuration.setSettings() method 
     */
    public void setFreemarkerSettings(Properties props) {
        freemarkerSettings = props;
    }
    
	/**
     * @param map a Map containing well-known FreeMarker objects
     * which will be passed to FreeMarker's Configuration.setSharedVariable() method 
     */
    public void setFreemarkerVariables(Map variables) {
        freemarkerVariables = variables;
    }
    
	/**
     * initialise the FreeMarker Configuration object with properties set within this
     * instance.  If a custom Configuration was set using setConfiguration(), then this
     * method will log a warning and do nothing.
     * @throws FreemarkerInitializationException if the FreeMarker configuration could
     * not be initialized for any reason
     */
    public synchronized void initConfiguration() throws FreemarkerInitializationException {
		if (isCustomConfiguration) {
			logger.warn("Attempt to initialise a custom Configuration instance was ignored");
			return;
		}
		
		// set the configuration from the FreeMarker default and modify as required
		this.configuration = Configuration.getDefaultConfiguration();
		Properties props = new Properties();

		// try default config location as fallback
		Resource actualLocation = this.configLocation;
		if (this.configLocation == null && this.freemarkerSettings == null) {
			actualLocation = getDefaultConfigLocation();
		}

		try {
			// load config file if set
			if (actualLocation != null) {
				logger.info("Loading FreeMarker config from [" + actualLocation + "]");
				InputStream is = actualLocation.getInputStream();
				try {
					props.load(is);
				}
				finally {
					is.close();
				}
			}

			// merge local properties if set
			if (this.freemarkerSettings != null) {
				props.putAll(this.freemarkerSettings);
			}
		
			// FreeMarker will only accept known keys in it's setSettings(Properties) method
			// and setSharedVariable() method
			if (props.size() > 0) 
				this.configuration.setSettings(props);
			if (freemarkerVariables != null && freemarkerVariables.size() > 0)
				this.configuration.setAllSharedVariables(new SimpleHash(freemarkerVariables));		
			
		}
		catch (IOException ex) {
			logger.info("Failed to find any FreeMarker configuration properties.  Default configuration will be assumed");
		}
		catch (TemplateException ex) {
			throw new FreemarkerInitializationException("Error setting FreeMarker configuration", ex);
		}
	}
	
	/**
	 * Return a default config location, if any. If neither "configLocation" nor
	 * "freemarkerProperties" is set, this will be used as
	 * config location. Default is none: can be overridden in subclasses.
	 */
	protected Resource getDefaultConfigLocation() {
		return null;
	}


}
