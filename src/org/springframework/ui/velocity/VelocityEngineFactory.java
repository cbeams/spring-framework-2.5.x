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

import org.springframework.core.io.Resource;

/**
 * Factory that configures a VelocityEngine in a Spring application context.
 * Typically, you will either use VelocityEngineFactoryBean for preparing a
 * VelocityEngine as bean reference, or VelocityConfigurer for web views.
 *
 * <p>The optional "configLocation" property sets the location of the Velocity
 * properties file, within the current application. The factory needs to run in
 * an application context to be able to load such a context resource.
 *
 * <p>Velocity properties can be overridden via "velocityProperties", or even
 * completely specified locally, avoiding the need for an external properties file.
 * This is the only way available when not running in an application context.
 *
 * <p>The "resourceLoaderPath" property can be used to specify the Velocity
 * resource loader path, relative to the application context. Like "configLocation",
 * this is just available in an application context.
 *
 * <p>If "overrideLogging" is true (the default), the VelocityEngine will be configured
 * to log via Commons Logging, i.e. using CommonsLoggingLogSystem as log system.
 *
 * <p>The simplest way to use this class in an application context is to specify just
 * a "resourceLoaderPath": the VelocityEngine does not need any more configuration then.
 * Outside an application context, locally defined "velocityProperties" that indicate
 * the resource loader to use achieve the same.
 *
 * @author Juergen Hoeller
 * @see #setConfigLocation
 * @see #setVelocityProperties
 * @see #setResourceLoaderPath
 * @see CommonsLoggingLogSystem
 * @see VelocityEngineFactoryBean
 * @see org.springframework.web.servlet.view.velocity.VelocityConfigurer
 */
public class VelocityEngineFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	private Resource configLocation;

	private Properties velocityProperties;

	private Resource resourceLoaderPath;

	private boolean overrideLogging = true;

	/** It's the job of this class to initialize and expose this */
	private VelocityEngine velocityEngine;


	/**
	 * Set location of the Velocity config file. Default value is
	 * is determined by getDefaultFileLocation, which will be applied
	 * <i>only</i> if "velocityProperties" is not set.
	 * @see #setVelocityProperties
	 * @see #getDefaultConfigLocation
	 */
	public void setConfigLocation(Resource configLocation) {
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
	 * Set the Velocity resource loader path, relative to the ApplicationContext.
	 * Only applicable when this factory runs in an ApplicationContext.
	 * <p>Will define a path for the default Velocity resource loader with the name
	 * "file", of type org.apache.velocity.runtime.resource.loader.FileResourceLoader,
	 * appending the given path to the application context resource base.
	 * @see org.springframework.context.ApplicationContext#getResource
	 * @see org.apache.velocity.runtime.resource.loader.FileResourceLoader
	 */
	public void setResourceLoaderPath(Resource resourceLoaderPath) {
		this.resourceLoaderPath = resourceLoaderPath;
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
	 * Prepare the VelocityEngine instance.
	 * @throws VelocityInitializationException on Velocity initialization failure
	 */
	public void initVelocityEngine() throws VelocityInitializationException {
		this.velocityEngine = newVelocityEngine();
		Properties props = new Properties();

		try {
			// try default config location as fallback
			Resource actualLocation = this.configLocation;
			if (this.configLocation == null && this.velocityProperties == null && this.resourceLoaderPath == null) {
				actualLocation = getDefaultConfigLocation();
			}

			// load config file if set
			if (actualLocation != null) {
				logger.info("Loading Velocity config from [" + actualLocation + "]");
				InputStream is = actualLocation.getInputStream();
				try {
					props.load(is);
				}
				finally {
					is.close();
				}
			}
		}
		catch (IOException ex) {
			throw new VelocityInitializationException("Error loading Velocity config from [" + this.configLocation + "]", ex);
		}

		// merge local properties if set
		if (this.velocityProperties != null) {
			props.putAll(this.velocityProperties);
		}

		// set properties
		for (Iterator it = props.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			this.velocityEngine.setProperty(key, props.getProperty(key));
		}

		try {
			// set a resource loader path, if required
			if (this.resourceLoaderPath != null) {
				if (!this.resourceLoaderPath.exists()) {
					throw new VelocityInitializationException("Specified resource loader path does not exist: " +
																										this.resourceLoaderPath);
				}
				this.velocityEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,
																				this.resourceLoaderPath.getFile().getAbsolutePath());
			}

			// log via Commons Logging?
			if (this.overrideLogging) {
				this.velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new CommonsLoggingLogSystem());
			}

			// perform initialization
			this.velocityEngine.init();
		}
		catch (VelocityInitializationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new VelocityInitializationException("Could not initialize Velocity engine", ex);
		}
	}

	/**
	 * Return a default config location, if any. If neither "configLocation" nor
	 * "velocityProperties" nor "resourceLoaderPath" is set, this will be used as
	 * config location. Default is none: can be overridden in subclasses.
	 */
	protected Resource getDefaultConfigLocation() throws IOException {
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
	 * Return the prepared VelocityEngine instance.
	 * @throws VelocityInitializationException on Velocity initialization failure
	 */
	public synchronized VelocityEngine getVelocityEngine() throws VelocityInitializationException {
		if (this.velocityEngine == null) {
			initVelocityEngine();
		}
		return this.velocityEngine;
	}

}
