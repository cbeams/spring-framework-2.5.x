/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ui.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;

import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.util.StringUtils;

/**
 * Factory that configures a VelocityEngine in a Spring application context.
 * Typically, you will either use VelocityEngineFactoryBean for preparing a
 * VelocityEngine as bean reference, or VelocityConfigurer for web views.
 *
 * <p>The optional "configLocation" property sets the configLocation within the WAR
 * of the Velocity properties file. By default it will be sought in the WEB-INF
 * directory, with the name "velocity.properties".
 *
 * <p>Velocity properties can be overridden via "velocityProperties", or even
 * completely specified locally, avoiding the need for an external properties file.
 *
 * <p>When using Velocity's FileResourceLoader, the "appRootMarker" mechanism can
 * be used to refer to the application context resource base within a Velocity
 * property value. Set the "appRootMarker" bean property to a placeholder like
 * "${app.root}" that gets replaced by the resource base path in the Velocity
 * property values before getting passed to Velocity.
 *
 * <p>Example Velocity properties that leverage the "appRootMarker" mechanism:
 * <p><code>
 * resource.loader=file<br>
 * file.resource.loader.class=org.apache.velocity.runtime.resource.loader.FileResourceLoader<br>
 * file.resource.loader.path=${app.root}/velocity
 * </code>
 *
 * <p>If "overrideLogging" is true (the default), the VelocityEngine will be configured
 * to log via Commons Logging, i.e. using CommonsLoggingLogSystem as log system.
 *
 * @author Juergen Hoeller
 * @see #setConfigLocation
 * @see #setVelocityProperties
 * @see CommonsLoggingLogSystem
 * @see VelocityEngineFactoryBean
 * @see org.springframework.web.servlet.view.velocity.VelocityConfigurer
 */
public class VelocityEngineFactory extends ApplicationObjectSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	private String configLocation;

	private Properties velocityProperties;

	private String appRootMarker;

	private boolean overrideLogging = true;

	/**
	 * It's the job of this class to initialize and expose this
	 */
	private VelocityEngine velocityEngine;

	/**
	 * Set location of the Velocity config file. Default value is
	 * is determined by getDefaultFileLocation, which will be applied
	 * <i>only</i> if "velocityProperties" is not set.
	 * @see #setVelocityProperties
	 * @see #getDefaultConfigLocation
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
	 * If Velocity should log via Commons Logging, i.e. if Velocity's
	 * log system should be set to CommonsLoggingLogSystem.
	 * Default value is true.
	 */
	public void setOverrideLogging(boolean overrideLogging) {
		this.overrideLogging = overrideLogging;
	}

	/**
	 * Set the marker that gets replaced with the resource base,
	 * i.e. root directory of the web application.
	 * Default value is "${app.root}".
	 */
	public void setAppRootMarker(String appRootMarker) {
		this.appRootMarker = appRootMarker;
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
				actualLocation = getDefaultConfigLocation();
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
				logger.warn("Cannot replace marker [" + this.appRootMarker + "] with resource base because the WAR file is not expanded");
			}

			// Set properties
			for (Iterator it = prop.keySet().iterator(); it.hasNext();) {
				String key = (String) it.next();
				String value = prop.getProperty(key);
				if (resourceBase != null) {
					value = StringUtils.replace(value, this.appRootMarker, resourceBase);
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
		catch (IOException ex) {
			throw new ApplicationContextException("Error loading Velocity config from [" + this.configLocation + "]", ex);
		}
		catch (Exception ex) {
			throw new ApplicationContextException(
				"Error initializing Velocity from properties file [" + this.configLocation + "]",
				ex);
		}
	}

	/**
	 * Return a default config location, if any. If neither a configLocation
	 * nor velocityProperties is set, this will be used as config location.
	 * <p>Default is none. Can be overridden in subclasses.
	 */
	protected String getDefaultConfigLocation() {
		return null;
	}

	/**
	 * Return a new VelocityEngine. Subclasses can override this for
	 * custom initialization, or tests can override it
	 */
	protected VelocityEngine newVelocityEngine() {
		return new VelocityEngine();
	}


	/**
	 * Return the prepared VelocityEngine after initialization.
	 */
	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

}
