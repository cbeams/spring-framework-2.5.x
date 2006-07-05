/*
 * Copyright 2002-2006 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * EntityResolver implementation that delegates to a BeansDtdResolver
 * and a PluggableSchemaResolver for DTDs and XML schemas, respectively.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeansDtdResolver
 * @see PluggableSchemaResolver
 */
public class DelegatingEntityResolver implements EntityResolver {

	/** Suffix for DTD files */
	public static final String DTD_SUFFIX = ".dtd";

	/** Suffix for schema definition files */
	public static final String XSD_SUFFIX = ".xsd";


	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private final EntityResolver dtdResolver;

	private final EntityResolver schemaResolver;


	/**
	 * Create a new DelegatingEntityResolver that delegates to
	 * a default BeansDtdResolver and a default PluggableSchemaResolver.
	 * <p>Configures the PluggableSchemaResolver with the supplied ClassLoader.
	 * @param classLoader the ClassLoader to use for loading
	 */
	public DelegatingEntityResolver(ClassLoader classLoader) {
		this.dtdResolver = new BeansDtdResolver();
		this.schemaResolver = new PluggableSchemaResolver(classLoader);
	}

	/**
	 * Create a new DelegatingEntityResolver that delegates to
	 * the given BeansDtdResolver and the given PluggableSchemaResolver.
	 * @param dtdResolver the EntityResolver to resolve DTDs with
	 * @param schemaResolver the EntityResolver to resolve XML schemas with
	 */
	public DelegatingEntityResolver(EntityResolver dtdResolver, EntityResolver schemaResolver) {
		this.dtdResolver = dtdResolver;
		this.schemaResolver = schemaResolver;
	}


	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (systemId != null) {
			if (systemId.endsWith(DTD_SUFFIX)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Attempting to resolve DTD [" + systemId + "] using [" +
							this.dtdResolver.getClass().getName() + "]");
				}
				return this.dtdResolver.resolveEntity(publicId, systemId);
			}
			else if (systemId.endsWith(XSD_SUFFIX)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Attempting to resolve XML Schema [" + systemId + "] using [" +
							this.schemaResolver.getClass().getName() + "]");
				}
				return this.schemaResolver.resolveEntity(publicId, systemId);
			}
		}

		return null;
	}

}
