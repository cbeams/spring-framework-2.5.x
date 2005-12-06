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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class PluggableSchemaResolver implements EntityResolver {

	private static final String SPRING_SCHEMA_PREFIX = "http://www.springframework.org/schema/";

	private static final String PACKAGE_PREFIX = "org/springframework/";


	public InputSource resolveEntity(String publicId, String systemId) throws IOException {
		if (systemId != null) {
			Resource schemaResource;
			if (isSpringSchema(systemId)) {
				// It's a Spring schema.
				schemaResource = resolveSpringSchema(systemId);
			}
			else {
				// Some 3rd party schema.
				schemaResource = resolveThirdPartySchema(systemId);
			}
			if (schemaResource != null) {
				InputSource source = new InputSource(schemaResource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				return source;
			}
		}
		return null;
	}

	private boolean isSpringSchema(String systemId) {
		return (systemId.indexOf(SPRING_SCHEMA_PREFIX) > -1);
	}

	private Resource resolveSpringSchema(String systemId) {
		String path = PACKAGE_PREFIX + systemId.substring(SPRING_SCHEMA_PREFIX.length());
		return new ClassPathResource(path);
	}

	private Resource resolveThirdPartySchema(String systemId) {
		throw new UnsupportedOperationException();
	}

}
