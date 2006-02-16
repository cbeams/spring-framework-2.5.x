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

import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;

/**
 * {@link EntityResolver} implementation that attempts to resolve schema URLs into
 * local {@link ClassPathResource classpath resources} using a set of mappings files.
 *
 * <p>By default, this class will look for mapping files using the pattern:
 * <code>META-INF/spring.schemas</code> allowing for multiple files to exist on the
 * classpath at any one time.
 *
 * <p>The pattern for the mapping files can be overidden using the
 * {@link #PluggableSchemaResolver(ClassLoader, String)} constructor
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class PluggableSchemaResolver implements EntityResolver {

	/**
	 * The location to look for the mapping files. Can be present in multiple
	 * JAR files.
	 */
	public static final String DEFAULT_SCHEMA_MAPPINGS_LOCATION = "META-INF/spring.schemas";


	/**
	 * Stores the mapping of schema URL -> local schema path.
	 */
	private Properties schemaMappings;


	/**
	 * Loads the schema URL -> schema file location mappings using the default
	 * mapping file pattern "META-INF/spring.schemas".
	 * @see PropertiesLoaderUtils#loadAllProperties(String, ClassLoader)
	 */
	public PluggableSchemaResolver(ClassLoader classLoader) {
		this(classLoader, DEFAULT_SCHEMA_MAPPINGS_LOCATION);
	}

	/**
	 * Loads the schema URL -> schema file location mappings using the given
	 * mapping file pattern.
	 * @see PropertiesLoaderUtils#loadAllProperties(String, ClassLoader)
	 */
	public PluggableSchemaResolver(ClassLoader classLoader, String schemaMappingsLocation) {
		Assert.notNull(classLoader, "'classLoader' cannot be null");
		Assert.hasText(schemaMappingsLocation, "'schemaMappingsLocation' cannot be null or empty");
		try {
			this.schemaMappings =
					PropertiesLoaderUtils.loadAllProperties(schemaMappingsLocation, classLoader);
		}
		catch (IOException e) {
			throw new FatalBeanException(
					"Unable to load schema mappings from location [" + schemaMappingsLocation + "].", e);
		}
	}


	public InputSource resolveEntity(String publicId, String systemId) throws IOException {
		if (systemId != null) {
			String resourceLocation = this.schemaMappings.getProperty(systemId);
			if (resourceLocation != null) {
				Resource resource = new ClassPathResource(resourceLocation);
				InputSource source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				return source;
			}
		}
		return null;
	}

}
