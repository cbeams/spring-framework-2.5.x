/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.util.WebUtils;

/**
 * Implementation of VelocityConfiguration interface.
 * JavaBean to configure Velocity, by setting the configLocation of the Velocity
 * properties file. This bean must be included in the application context of any
 * application using Velocity with the Spring Framework.
 *
 * <p>The optional "configLocation" property sets the configLocation within the WAR
 * of the Velocity properties file. By default it will be sought in the WEB-INF
 * directory, with the name "velocity.properties".
 *
 * <p>Velocity properties can be overridden via "velocityProperties", or even
 * completely specified locally, avoiding the need for an external properties file.
 *
 * <p>When using Velocity's FileResourceLoader, the "webAppRootMarker" mechanism can
 * be used to refer to the web app resource base within a Velocity property value.
 * By default, the marker "${webapp.root}" gets replaced with the web app root
 * directory. Note that this will only work with expanded WAR files.
 *
 * <p>This bean exists purely to configure Velocity. It exposes no methods other than
 * initialization methods, and is not meant to be referenced by application components.
 * It exposes the VelocityEngine via its implementation of the
 * VelocityConfiguration interface.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see VelocityView
 * @version $Id: VelocityConfigurer.java,v 1.4 2003-09-20 20:44:17 johnsonr Exp $
 */
public class VelocityConfigurer extends WebApplicationObjectSupport implements VelocityConfiguration {

	public static final String DEFAULT_CONFIG_LOCATION = "WEB-INF/velocity.properties";

	public static final String DEFAULT_WEB_APP_ROOT_MARKER = "${" + WebUtils.DEFAULT_WEB_APP_ROOT_KEY + "}";

	protected final Log logger = LogFactory.getLog(getClass());

	private String configLocation;

	private Properties velocityProperties;

	private String webAppRootMarker = DEFAULT_WEB_APP_ROOT_MARKER;

	private boolean overrideLogging = true;
	
	/**
	 * It's the job of this class to initialize and expose this
	 */
	private VelocityEngine velocityEngine;

	/**
	 * Set location of the Velocity config file. Default value is
	 * "WEB-INF/velocity.properties", which will be applied
	 * <i>only</i> if "velocityProperties" is not set.
	 * @see #setVelocityProperties
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set Velocity properties, like "resource loader".
	 * <p>Can be used to override values in a Velocity config file,
	 * or to specify all necessary properties locally.
	 * @see #setConfigLocation
	 */
	public void setVelocityProperties(Properties velocityProperties) {
		this.velocityProperties = velocityProperties;
	}

	/**
	 * If Velocity should log via Commons Logging, i.e. if logging
	 * should be overridden with CommonsLoggingLogSystem.
	 * Default value is true.
	 */
	public void setOverrideLogging(boolean overrideLogging) {
		this.overrideLogging = overrideLogging;
	}

	/**
	 * Set the marker that gets replaced with the resource base,
	 * i.e. root directory of the web application.
	 * Default value is "${webapp.root}".
	 */
	public void setWebAppRootMarker(String webAppRootMarker) {
		this.webAppRootMarker = webAppRootMarker;
	}
	
	/**
	 * @return a new VelocityEngine. Subclasses can override this for custom initialization,
	 * or tests can override it
	 */
	protected VelocityEngine newVelocityEngine() {
		return new VelocityEngine();
	}

	/**
	 * Initializes the Velocity runtime.
	 */
	protected void initApplicationContext() throws ApplicationContextException {
		
		this.velocityEngine = newVelocityEngine();
		
		try {
			Properties prop = new Properties();
			// try default config location as fallback
			String actualLocation = this.configLocation;
			if (this.configLocation == null && this.velocityProperties == null) {
				actualLocation = DEFAULT_CONFIG_LOCATION;
			}
			// load config file if set
			if (actualLocation != null) {
				logger.info("Loading Velocity config from [" + actualLocation + "]");
				InputStream is = getApplicationContext().getResourceAsStream(actualLocation);
				if (is == null) {
					throw new ApplicationContextException("Velocity properties file not found within WAR at '" + actualLocation + "'");
				}
				prop.load(is);
			}
			// merge local properties if set
			if (this.velocityProperties != null) {
				prop.putAll(this.velocityProperties);
			}
			// determine the root directory of the web app
			String resourceBase = getApplicationContext().getResourceBasePath();
			if (resourceBase == null) {
				logger.warn("Cannot replace marker [" + this.webAppRootMarker + "] with resource base because the WAR file is not expanded");
			}
			
			// Set properties
			for (Iterator it = prop.keySet().iterator(); it.hasNext();) {
				String key = (String) it.next();
				String value = prop.getProperty(key);
				if (resourceBase != null) {
					value = StringUtils.replace(value, this.webAppRootMarker, resourceBase);
				}
				this.velocityEngine.setProperty(key, value);
			}
			// log via Commons Logging?
			if (this.overrideLogging) {
				this.velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new CommonsLoggingLogSystem());
			}
			// perform initialization
			this.velocityEngine.init();
		}
		catch (ServletException ex) {
			throw new ApplicationContextException("Error loading Velocity config from [" + this.configLocation + "]", ex);
		}
		catch (IOException ex) {
			throw new ApplicationContextException("Error loading Velocity config from [" + this.configLocation + "]", ex);
		}
		catch (Exception ex) {
			throw new ApplicationContextException(
				"Error initializing Velocity from properties file [" + this.configLocation + "] (loaded OK)",
				ex);
		}
	}
	

	/**
	 * @see org.springframework.web.servlet.view.velocity.VelocityConfiguration#getVelocityEngine()
	 */
	public VelocityEngine getVelocityEngine() {
		return this.velocityEngine;
	}

}
