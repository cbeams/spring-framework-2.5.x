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

package org.springframework.beans.factory.xml;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * EntityResolver implementation for the Spring beans DTD,
 * to load the DTD from the Spring classpath resp. JAR file.
 *
 * <p>Fetches "spring-beans.dtd" from the classpath resource
 * "/org/springframework/beans/factory/xml/spring-beans.dtd",
 * no matter if specified as some local URL or as
 * "http://www.springframework.org/dtd/spring-beans.dtd".
 *
 * @author Juergen Hoeller
 * @since 04.06.2003
 */
public class BeansDtdResolver implements EntityResolver {

	private static final String DTD_NAME = "spring-beans";

	private static final String SEARCH_PACKAGE = "/org/springframework/beans/factory/xml/";

	protected final Log logger = LogFactory.getLog(getClass());

	public InputSource resolveEntity(String publicId, String systemId) throws IOException {
		logger.debug("Trying to resolve XML entity with public ID [" + publicId +
								 "] and system ID [" + systemId + "]");
		if (systemId != null && systemId.indexOf(DTD_NAME) > systemId.lastIndexOf("/")) {
			String dtdFile = systemId.substring(systemId.indexOf(DTD_NAME));
			logger.debug("Trying to locate [" + dtdFile + "] under [" + SEARCH_PACKAGE + "]");
			try {
				Resource resource = new ClassPathResource(SEARCH_PACKAGE + dtdFile, getClass());
				InputSource source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				logger.debug("Found beans DTD [" + systemId + "] in classpath");
				return source;
			}
			catch (IOException ex) {
				logger.debug("Could not resolve beans DTD [" + systemId + "]: not found in classpath", ex);
			}
		}
		// use the default behaviour -> download from website or wherever
		return null;
	}

}
