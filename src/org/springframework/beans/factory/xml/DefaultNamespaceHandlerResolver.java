/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Default implementation of the {@link NamespaceHandler}. Resolves namespace URIs
 * to implementation classes based on the mappings contained in mapping file.
 *
 * <p>By default, this implementation looks for the mapping file at
 * <code>META-INF/spring.handlers</code>, but this can be changed using the
 * {@link #DefaultNamespaceHandlerResolver(String, ClassLoader)} constructor.
 *
 * @author Rob Harrop
 * @see NamespaceHandler
 * @see DefaultXmlBeanDefinitionParser
 * @since 2.0
 */
public class DefaultNamespaceHandlerResolver implements NamespaceHandlerResolver {

	/**
	 * The location to look for the mapping files. Can be present in multiple
	 * JAR files.
	 */
	private static final String SPRING_HANDLER_MAPPINGS_LOCATION = "META-INF/spring.handlers";


	/**
	 * <code>Log</code> instance of this class.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The currently configured mapping file location.
	 */
	private String handlerMappingsLocation = SPRING_HANDLER_MAPPINGS_LOCATION;

	/**
	 * Stores the mapping of namespace URI -> NamespaceHandler instances.
	 */
	private Map handlerMappings;

	/**
	 * {@link ClassLoader} instance used to load mapping resources.
	 */
	private ClassLoader classLoader;


	/**
	 * Create a new <code>DefaultNamespaceHandlerResolver</code> using the
	 * default mapping file location.
	 * @see #SPRING_HANDLER_MAPPINGS_LOCATION
	 */
	public DefaultNamespaceHandlerResolver(ClassLoader classLoader) {
		this.classLoader = classLoader;
		initHandlerMappings();
	}

	/**
	 * Create a new <code>DefaultNamespaceHandlerResolver</code> using the
	 * supplied mapping file location.
	 */
	public DefaultNamespaceHandlerResolver(String handlerMappingsLocation, ClassLoader classLoader) {
		Assert.notNull(handlerMappingsLocation, "'handlerMappingsLocation' cannot be null");
		this.handlerMappingsLocation = handlerMappingsLocation;
		this.classLoader = classLoader;
		initHandlerMappings();
	}


	/**
	 * Load the namespace URI -> <code>NamespaceHandler</code> class mappings from the configured
	 * mapping file. Converts the class names into actual class instances and checks that
	 * they implement the <code>NamespaceHandler</code> interface. Pre-instantiates an instance
	 * of each <code>NamespaceHandler</code> and maps that instance to the corresponding
	 * namespace URI.
	 */
	private void initHandlerMappings() {
		Properties mappings = loadMappings();
		this.handlerMappings = new HashMap(mappings.size());
		for (Enumeration en = mappings.propertyNames(); en.hasMoreElements();) {
			String namespaceUri = (String) en.nextElement();
			String className = mappings.getProperty(namespaceUri);
			try {
				Class handlerClass = ClassUtils.forName(className, this.classLoader);
				if (!NamespaceHandler.class.isAssignableFrom(handlerClass)) {
					throw new IllegalArgumentException("Class [" + className +
							"] does not implement the NamespaceHandler interface");
				}
				NamespaceHandler namespaceHandler = (NamespaceHandler) BeanUtils.instantiateClass(handlerClass);
				namespaceHandler.init();
				this.handlerMappings.put(namespaceUri, namespaceHandler);
			}
			catch (ClassNotFoundException ex) {
				if (logger.isInfoEnabled()) {
					logger.info("Ignoring handler [" + className + "]: class not found");
				}
			}
		}
	}

	private Properties loadMappings() {
		try {
			return PropertiesLoaderUtils.loadAllProperties(this.handlerMappingsLocation, this.classLoader);
		}
		catch (IOException ex) {
			throw new FatalBeanException("Unable to load NamespaceHandler mappings using mapping location ["
					+ this.handlerMappingsLocation + "].", ex);
		}
	}

	/**
	 * Locate the {@link NamespaceHandler} for the supplied namespace URI from the configured mappings.
	 */
	public NamespaceHandler resolve(String namespaceUri) {
		NamespaceHandler namespaceHandler = (NamespaceHandler) this.handlerMappings.get(namespaceUri);
		if (namespaceUri == null) {
			throw new IllegalArgumentException(
					"Unable to locate NamespaceHandler for namespace URI [" + namespaceUri + "]");
		}
		return namespaceHandler;
	}

}
