/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ui.freemarker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Factory that configures a FreeMarker Configuration. Can be used standalone, but
 * typically you will either use FreeMarkerConfigurationFactoryBean for preparing a
 * Configuration as bean reference, or FreeMarkerConfigurer for web views.
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
 * <p>Note: Spring's FreeMarker support requires FreeMarker 2.3 or higher.
 *
 * @author Darren Davison
 * @author Juergen Hoeller
 * @since 3/3/2004
 * @see #setConfigLocation
 * @see #setFreemarkerSettings
 * @see #setFreemarkerVariables
 * @see #setTemplateLoaderPath
 * @see #createConfiguration
 */
public class FreeMarkerConfigurationFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	private Resource configLocation;

	private Properties freemarkerSettings;

	private Map freemarkerVariables;

	private String templateLoaderPath;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();


	/**
	 * Set the location of the FreeMarker config file.
	 * Alternatively, you can specify all setting locally.
	 * @see #setFreemarkerSettings
	 * @see #setTemplateLoaderPath
	 */
	public void setConfigLocation(Resource resource) {
		configLocation = resource;
	}

	/**
	 * Set properties that contain well-known FreeMarker keys which will be
	 * passed to FreeMarker's Configuration.setSettings method.
	 * @see freemarker.template.Configuration#setSettings
	 */
	public void setFreemarkerSettings(Properties settings) {
		freemarkerSettings = settings;
	}

	/**
	 * Set a Map that contains well-known FreeMarker objects which will be
	 * passed to FreeMarker's Configuration.setAllSharedVariables method.
	 * @see freemarker.template.Configuration#setAllSharedVariables
	 */
	public void setFreemarkerVariables(Map variables) {
		freemarkerVariables = variables;
	}

	/**
	 * Set the Freemarker template loader path via a Spring resource location.
	 * <p>When populated via a String, standard URLs like "file:" and "classpath:"
	 * pseudo URLs are supported, as understood by ResourceEditor. Allows for
	 * relative paths when running in an ApplicationContext.
	 * <p>Will define a path for the default FreeMarker template loader.
	 * If the specified resource cannot be resolved to a java.io.File, the
	 * generic SpringTemplateLoader will be used, without modification detection.
	 * @see org.springframework.core.io.ResourceEditor
	 * @see org.springframework.context.ApplicationContext#getResource
	 * @see freemarker.template.Configuration#setDirectoryForTemplateLoading
	 * @see SpringTemplateLoader
	 */
	public void setTemplateLoaderPath(String templateLoaderPath) {
		this.templateLoaderPath = templateLoaderPath;
	}
    
	/**
	 * Set the Spring ResourceLoader to use for loading FreeMarker template files.
	 * The default is DefaultResourceLoader. Will get overridden by the
	 * ApplicationContext if running in a context.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}


	/**
	 * Prepare the FreeMarker Configuration and return it.
	 * @return the FreeMarker Configuration object
	 * @throws IOException if the config file wasn't found
	 * @throws TemplateException on FreeMarker initialization failure
	 */
	public Configuration createConfiguration() throws IOException, TemplateException {
		Configuration config = newConfiguration();
		Properties props = new Properties();

		// load config file if set
		if (this.configLocation != null) {
			logger.info("Loading FreeMarker config from [" + this.configLocation + "]");
			InputStream is = this.configLocation.getInputStream();
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

		// FreeMarker will only accept known keys in its setSettings and
		// setAllSharedVariables methods
		if (!props.isEmpty()) {
			config.setSettings(props);
		}
		if (this.freemarkerVariables != null && this.freemarkerVariables.size() > 0) {
			config.setAllSharedVariables(new SimpleHash(this.freemarkerVariables));
		}

		if (this.templateLoaderPath != null) {
			Resource path = this.resourceLoader.getResource(this.templateLoaderPath);
			try {
				config.setDirectoryForTemplateLoading(path.getFile());
			}
			catch (IOException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Cannot resolve template loader path [" + this.templateLoaderPath +
											 "] to File: using SpringTemplateLoader", ex);
				}
				else if (logger.isInfoEnabled()) {
					logger.info("Cannot resolve template loader path [" + this.templateLoaderPath +
											"] to File: using SpringTemplateLoader");
				}
				config.setTemplateLoader(new SpringTemplateLoader(this.resourceLoader,
																													this.templateLoaderPath));
			}
		}

		postProcessConfiguration(config);
		return config;
	}

	/**
	 * Return a new Configuration object. Subclasses can override this for
	 * custom initialization, or for using a mock object for testing.
	 * Called by createConfiguration.
	 * @return the Configuration object
	 * @throws IOException if a config file wasn't found
	 * @throws TemplateException on FreeMarker initialization failure
	 * @see #createConfiguration
	 */
	protected Configuration newConfiguration() throws IOException, TemplateException {
		return new Configuration();
	}

	/**
	 * To be implemented by subclasses that want to to perform custom
	 * post-processing of the Configuration object after this FactoryBean
	 * performed its default initialization.
	 * Called by createConfiguration.
	 * @param config the current Configuration object
	 * @throws IOException if a config file wasn't found
	 * @throws TemplateException on FreeMarker initialization failure
	 * @see #createConfiguration
	 */
	protected void postProcessConfiguration(Configuration config)
			throws IOException, TemplateException {
	}

}
