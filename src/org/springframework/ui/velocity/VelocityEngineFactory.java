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
import org.apache.velocity.exception.VelocityException;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Factory that configures a VelocityEngine. Can be used standalone, but
 * typically you will either use VelocityEngineFactoryBean for preparing a
 * VelocityEngine as bean reference, or VelocityConfigurer for web views.
 *
 * <p>The optional "configLocation" property sets the location of the Velocity
 * properties file, within the current application. Velocity properties can be
 * overridden via "velocityProperties", or even completely specified locally,
 * avoiding the need for an external properties file.
 *
 * <p>The "resourceLoaderPath" property can be used to specify the Velocity
 * resource loader path via Spring's Resource abstraction, possibly relative
 * to the Spring application context.
 *
 * <p>If "overrideLogging" is true (the default), the VelocityEngine will be configured
 * to log via Commons Logging, i.e. using CommonsLoggingLogSystem as log system.
 *
 * <p>The simplest way to use this class is to specify just a "resourceLoaderPath":
 * The VelocityEngine does not need any further configuration then.
 *
 * @author Juergen Hoeller
 * @see #setConfigLocation
 * @see #setVelocityProperties
 * @see #setResourceLoaderPath
 * @see #setOverrideLogging
 * @see #createVelocityEngine
 * @see CommonsLoggingLogSystem
 * @see VelocityEngineFactoryBean
 * @see org.springframework.web.servlet.view.velocity.VelocityConfigurer
 */
public class VelocityEngineFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	private Resource configLocation;

	private Properties velocityProperties;

	private String resourceLoaderPath;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	private boolean overrideLogging = true;


	/**
	 * Set the location of the Velocity config file.
	 * Alternatively, you can specify all properties locally.
	 * @see #setVelocityProperties
	 * @see #setResourceLoaderPath
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set Velocity properties, like "file.resource.loader.path".
	 * <p>Can be used to override values in a Velocity config file,
	 * or to specify all necessary properties locally.
	 * @see #setConfigLocation
	 */
	public void setVelocityProperties(Properties velocityProperties) {
		this.velocityProperties = velocityProperties;
	}

	/**
	 * Set the Velocity resource loader path via a Spring resource location.
	 * <p>When populated via a String, standard URLs like "file:" and "classpath:"
	 * pseudo URLs are supported, as understood by ResourceEditor. Allows for
	 * relative paths when running in an ApplicationContext.
	 * <p>Will define a path for the default Velocity resource loader with the name
	 * "file". If the specified resource cannot be resolved to a java.io.File, the
	 * generic SpringResourceLoader will be used, without modification detection.
	 * @see org.springframework.core.io.ResourceEditor
	 * @see org.springframework.context.ApplicationContext#getResource
	 * @see org.apache.velocity.runtime.resource.loader.FileResourceLoader
	 * @see SpringResourceLoader
	 */
	public void setResourceLoaderPath(String resourceLoaderPath) {
		this.resourceLoaderPath = resourceLoaderPath;
	}

	/**
	 * Set the Spring ResourceLoader to use for loading Velocity template files.
	 * The default is DefaultResourceLoader. Will get overridden by the
	 * ApplicationContext if running in a context.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * If Velocity should log via Commons Logging, i.e. if Velocity's log system
	 * should be set to CommonsLoggingLogSystem. Default value is true.
	 * @see CommonsLoggingLogSystem
	 */
	public void setOverrideLogging(boolean overrideLogging) {
		this.overrideLogging = overrideLogging;
	}


	/**
	 * Prepare the VelocityEngine instance and return it.
	 * @return the VelocityEngine instance
	 * @throws IOException if the config file wasn't found
	 * @throws VelocityException on Velocity initialization failure
	 */
	public VelocityEngine createVelocityEngine() throws IOException, VelocityException {
		VelocityEngine velocityEngine = newVelocityEngine();
		Properties props = new Properties();

		// load config file if set
		if (this.configLocation != null) {
			logger.info("Loading Velocity config from [" + this.configLocation + "]");
			InputStream is = this.configLocation.getInputStream();
			try {
				props.load(is);
			}
			finally {
				is.close();
			}
		}

		// merge local properties if set
		if (velocityProperties != null) {
			props.putAll(velocityProperties);
		}

		// set properties
		for (Iterator it = props.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			velocityEngine.setProperty(key, props.getProperty(key));
		}

		// set a resource loader path, if required
		if (this.resourceLoaderPath != null) {
			try {
				Resource path = this.resourceLoader.getResource(this.resourceLoaderPath);
				velocityEngine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,
																				path.getFile().getAbsolutePath());
			}
			catch (IOException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Cannot resolve resource loader path [" + this.resourceLoaderPath +
											 "] to File: using SpringResourceLoader", ex);
				}
				else if (logger.isInfoEnabled()) {
					logger.info("Cannot resolve resource loader path [" + this.resourceLoaderPath +
											"] to File: using SpringResourceLoader");
				}
				velocityEngine.setProperty(VelocityEngine.RESOURCE_LOADER,
																				SpringResourceLoader.NAME);
				velocityEngine.setProperty(SpringResourceLoader.SPRING_RESOURCE_LOADER_CLASS,
																				SpringResourceLoader.class.getName());
				velocityEngine.setApplicationAttribute(SpringResourceLoader.SPRING_RESOURCE_LOADER,
																										this.resourceLoader);
				velocityEngine.setApplicationAttribute(SpringResourceLoader.SPRING_RESOURCE_LOADER_PATH,
																										this.resourceLoaderPath);
			}
		}

		// log via Commons Logging?
		if (this.overrideLogging) {
			velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new CommonsLoggingLogSystem());
		}

		try {
			// perform initialization
			velocityEngine.init();
		}
		catch (Exception ex) {
			logger.error("Why does VelocityEngine throw a generic checked exception, after all?", ex);
			throw new VelocityException(ex.getMessage());
		}

		return velocityEngine;
	}

	/**
	 * Return a new VelocityEngine. Subclasses can override this for
	 * custom initialization, or for using a mock object for testing.
	 */
	protected VelocityEngine newVelocityEngine() {
		return new VelocityEngine();
	}

}
