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
import java.util.Properties;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.PropertiesMergeUtils;
import org.springframework.util.Assert;
import org.springframework.beans.FatalBeanException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link EntityResolver} implementation that attempts to resolve schema URLs into
 * local {@link ClassPathResource classpath resources} using a set of mappings files.
 * <p/>
 * By default, this class will look for mapping files using the pattern:
 * <code>META-INF/spring.schemas</code> allowing for multiple files to exist on the
 * classpath at any one time.
 * <p/>
 * The pattern for the mapping files can be overidden using the
 * {@link #PluggableSchemaResolver(String, ClassLoader)} constructor
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class PluggableSchemaResolver implements EntityResolver {

	/**
	 * The location to look for the mapping files. Can be present in multiple
	 * JAR files.
	 */
	private static final String SPRING_SCHEMA_MAPPINGS_LOCATION = "META-INF/spring.schemas";

	/**
	 * <code>Log</code> instance of this class.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The currently configured mapping file location.
	 */
	private String schemaMappingsLocation = SPRING_SCHEMA_MAPPINGS_LOCATION;

	/**
	 * Stores the mapping of schema URL -> local schema path.
	 */
	private Properties schemaMappings;

	/**
	 * {@link ClassLoader} instance used to load mapping resources.
	 */
	private ClassLoader classLoader;

	public PluggableSchemaResolver(ClassLoader classLoader) {
		Assert.notNull(classLoader, "'classLoader' cannot be null.");
		this.classLoader = classLoader;
		initMappings();
	}

	public PluggableSchemaResolver(String schemaMappingsLocation, ClassLoader classLoader) {
		Assert.hasText(schemaMappingsLocation, "'schemaMappingsLocation' cannot be null or empty.");
		Assert.notNull(classLoader, "'classLoader' cannot be null.");
		this.schemaMappingsLocation = schemaMappingsLocation;
		this.classLoader = classLoader;
		initMappings();
	}

	public InputSource resolveEntity(String publicId, String systemId) throws IOException {
		if (systemId != null) {
			String resourceLocation = this.schemaMappings.getProperty(systemId);

			if(resourceLocation != null) {
				Resource resource = new ClassPathResource(resourceLocation);
				InputSource source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				return source;
			}
		}
		return null;
	}

	/**
	 * Loads the schema URL -> schema file location mappings using the configured mapping
	 * file pattern.
	 * @see PropertiesMergeUtils#findMergedProperties(String, ClassLoader) 
	 */
	private void initMappings() {
		try {
			this.schemaMappings = PropertiesMergeUtils.findMergedProperties(this.schemaMappingsLocation, this.classLoader);
		}
		catch (IOException e) {
			throw new FatalBeanException("Unable to load schema mappings from location [" + this.schemaMappingsLocation + "].", e);
		}

	}

}
